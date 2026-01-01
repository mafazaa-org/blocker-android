package com.mafazaa.ainaa.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScreenNode
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.MyNotificationManager
import com.mafazaa.ainaa.helpers.ScreenAnalyser
import com.mafazaa.ainaa.utils.Constants.browserPackages
import com.mafazaa.ainaa.utils.Constants.socialMediaPackages
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.MyLog.logUiTree
import com.mafazaa.ainaa.utils.isKeyguardSecure
import com.mafazaa.ainaa.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.measureTimedValue

@SuppressLint("AccessibilityPolicy")
class MyAccessibilityService : AccessibilityService() {
    val lockOverlayManager: LockOverlayManager by inject(LockOverlayManager::class.java)
    internal val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    internal var alarmManager: AlarmManager? = null
    internal var watchdogPendingIntent: PendingIntent? = null
    internal val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
    private val scriptRepo: ScriptRepo by inject(ScriptRepo::class.java)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            when (intent?.action) {
                ACTION_START_FOREGROUND -> {
                    MyNotificationManager.startForegroundService(this)
                    MyLog.i(TAG, "Accessibility Service started in foreground.")
                    isRunning = true
                }


                ACTION_STOP -> {
                    isRunning = false
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()

                }

                ACTION_SHARE_CURRENT_SCREEN -> {
                    if (!isRunning) {
                        MyLog.w(TAG, "Service not running, cannot share screen")
                    }
                    serviceScope.launch {
                        try {
                            rootInActiveWindow?.let {
                                val screenAnalysis = ScreenAnalyser.analyzeScreen(
                                    it, getString(R.string.app_name)
                                )
                                shareFile(logUiTree("screenShot", screenAnalysis))
                            }
                        } catch (e: Exception) {
                            MyLog.e(TAG, "Error sharing current screen: ${e.message}", e)
                        }
                    }
                }

                else -> {
                    isRunning = true
                    MyLog.w(TAG, "Unknown action received: ${intent?.action}")
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onStartCommand: ${e.message}", e)
        }
        return START_STICKY
    }

    companion object {
        fun Context.startAccessibilityService(action: String = ACTION_START_FOREGROUND) {
            try {
                val intent = Intent(this, MyAccessibilityService::class.java).apply {
                    this@apply.action = action
                }
                startService(intent)
            } catch (e: Exception) {
                MyLog.e(TAG, "Error starting accessibility service: ${e.message}", e)
            }
        }

        const val ACTION_STOP = "STOP_ACCESSIBILITY"
        var isRunning = false

        const val ACTION_START_FOREGROUND = "START_ACCESSIBILITY_FOREGROUND"
        const val ACTION_SHARE_CURRENT_SCREEN = "SHARE_CURRENT_SCREEN"
        internal const val NOTIFICATION_ID = 101 // Unique ID for the notification
        internal const val NOTIFICATION_CHANNEL_ID = "AINAA_PROTECTION_CHANNEL"

        const val TAG = "MyAccessibilityService"

        private val SETTINGS_PACKAGE = DeviceUtils.settingsPackageName
        private const val WATCHDOG_INTERVAL = 5 * 60 * 1000L // 5 minutes
    }

    override fun onCreate() {
        try {
            super.onCreate()
            MyLog.i(TAG, "Accessibility Service created.")
            scheduleWatchdog()
            ServiceMonitorJobService.scheduleJob(this)
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onCreate: ${e.message}", e)
        }
    }

    override fun onServiceConnected() {
        try {
            super.onServiceConnected()
            val info = AccessibilityServiceInfo()
            info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            serviceInfo = info
            MyLog.i(TAG, "Accessibility Service connected.")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onServiceConnected: ${e.message}", e)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            // Return early if event is null or service is not running
            event ?: return
            if (!isRunning) return
            if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            ) {
                return
            }
            rootInActiveWindow?.let { rootNode ->
                if (rootNode.packageName == "com.mafazaa.ainaa") {
                    return
                }

                serviceScope.launch {
                    try {
                        // Analyze the current screen and measure the time taken
                        val (analysisResult, analysisDuration) = measureTimedValue {
                            ScreenAnalyser.analyzeScreen(rootNode, getString(R.string.app_name))
                        }
                        Log.d(
                            TAG,
                            "Screen analyzed in ${analysisDuration.inWholeMilliseconds}ms, nodes=${analysisResult.nodesCount}"
                        )
                        val currentPackage = analysisResult.pkg
                        if (checkBlockedApp(currentPackage)) {
                            MyLog.i(TAG, "Blocked app in use: $currentPackage")
                            block(BlockReason.UsingBlockedApp(currentPackage ?: "unknown"))
                            return@launch
                        }
                        if ((socialMediaPackages + browserPackages).contains(analysisResult.pkg)) {
                            checkBlockedWords(analysisResult)?.let { blockedWord ->
                                MyLog.i(TAG, "Blocked word detected: $blockedWord")
                                block(blockedWord)
                                return@launch
                            }
                        }
                        // Evaluate scripts and measure the time taken
                        val (scriptResult, scriptEvalDuration) = measureTimedValue {
                            scriptRepo.evaluate(
                                analysisResult
                            )
                        }
                        Log.d(TAG, "Script evaluated in ${scriptEvalDuration.inWholeMilliseconds}ms")
                        when (scriptResult) {
                            is ScriptResult.Error -> {
                                MyLog.e(TAG, "Script evaluation error: ${scriptResult.error}")
                            }

                            is ScriptResult.Success -> {
                                if (scriptResult.matched) {
                                    MyLog.i(
                                        TAG,
                                        "Blocking due to script match: ${scriptResult.scriptName} on ${analysisResult.pkg}"
                                    )
                                    block(
                                        BlockReason.TryingToDisable(
                                            scriptResult.scriptName,
                                            analysisResult
                                        )
                                    )
                                    return@launch
                                }
                            }
                        }
                    } catch (e: Exception) {
                        MyLog.e(TAG, "Error processing accessibility event: ${e.message}", e)
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onAccessibilityEvent: ${e.message}", e)
        }
    }

    fun MyAccessibilityService.block(reason: BlockReason) {
        try {
            this.serviceScope.launch(Dispatchers.Main) {
                try {
                    lockOverlayManager.showOverlay(reason)
                } catch (e: Exception) {
                    MyLog.e(TAG, "Error showing overlay: ${e.message}", e)
                }
            }
            this.performGlobalAction(GLOBAL_ACTION_BACK)
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in block function: ${e.message}", e)
        }
    }


    fun MyAccessibilityService.checkBlockedApp(currentApp: String?): Boolean {
        return try {
            if (!this.isKeyguardSecure())//this means the phone is locked and the local data is encrypted
                return false
            currentApp != null &&
                    currentApp in sharedPrefs.blockedApps
        } catch (e: Exception) {
            MyLog.e(TAG, "Error checking blocked app: ${e.message}", e)
            false
        }
    }

    suspend fun MyAccessibilityService.checkBlockedWords(screenAnalysis: ScreenAnalysis): BlockReason.BlockedWordDetected? {
        return try {
            val maxNodes = 500
            if (!this.isKeyguardSecure())//this means the phone is locked and the local data is encrypted
                return null
            if (sharedPrefs.blockedWords.isEmpty()) {
                return null
            }
            withContext(Dispatchers.Default) {
                for (word in sharedPrefs.blockedWords) {
                    var stack = emptyList<ScreenNode>().toMutableList()
                    stack.add(screenAnalysis.root)
                    var nodesChecked = 0
                    while (stack.isNotEmpty()) {
                        val node = stack.removeAt(stack.size - 1)
                        val nodeText = node.text ?: ""
                        if (nodeText.split(" ").any { it.equals(word, true) }) {
                            MyLog.i(
                                TAG,
                                "Blocked word '$word' found in node text: '$nodeText'"
                            )
                            return@withContext BlockReason.BlockedWordDetected(
                                word,
                                nodeText
                            )
                        }
                        stack.addAll(node.children)
                        nodesChecked++
                        if (nodesChecked > maxNodes) {
                            MyLog.d(
                                TAG,
                                "Max nodes checked ($maxNodes), stopping search for blocked words."
                            )
                            break
                        }
                    }
                }
                return@withContext null
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error checking blocked words: ${e.message}", e)
            null
        }
    }

    private fun scheduleWatchdog() {
        try {
            if (alarmManager == null) {
                alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            }

            val intent = Intent(this, ServiceRestartReceiver::class.java).apply {
                action = ServiceRestartReceiver.ACTION_RESTART_SERVICE
            }

            watchdogPendingIntent = PendingIntent.getBroadcast(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            val triggerTime = SystemClock.elapsedRealtime() + WATCHDOG_INTERVAL

            // Use setExactAndAllowWhileIdle for better reliability on Doze mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    watchdogPendingIntent!!
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    watchdogPendingIntent!!
                )
            }

            MyLog.d(TAG, "Watchdog alarm scheduled")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error scheduling watchdog: ${e.message}", e)
        }
    }

    override fun onInterrupt() {
        try {
            serviceScope.cancel()
            MyLog.w(TAG, "Service interrupted")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onInterrupt: ${e.message}", e)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            MyLog.w(TAG, "Task removed, scheduling restart")

            // Send broadcast to restart service
            val restartIntent = Intent(ServiceRestartReceiver.ACTION_RESTART_SERVICE)
            restartIntent.setClass(this, ServiceRestartReceiver::class.java)
            sendBroadcast(restartIntent)

            // Reschedule watchdog
            scheduleWatchdog()

            MyLog.d(TAG, "Restart broadcast sent")
            super.onTaskRemoved(rootIntent)
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onTaskRemoved: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        try {
            MyLog.w(TAG, "Service being destroyed, scheduling restart")

            // Send broadcast to restart service
            val restartIntent = Intent(ServiceRestartReceiver.ACTION_RESTART_SERVICE)
            restartIntent.setClass(this, ServiceRestartReceiver::class.java)
            sendBroadcast(restartIntent)

            // Schedule watchdog alarm
            scheduleWatchdog()

            // Cancel coroutine scope
            serviceScope.cancel()

            super.onDestroy()
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onDestroy: ${e.message}", e)
        }
    }
}
