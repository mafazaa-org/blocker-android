package com.mafazaa.ainaa.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.helpers.DeviceUtils
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.ScreenAnalyser
import com.mafazaa.ainaa.receiver.RestartReceiver
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.MyLog.logUiTree
import com.mafazaa.ainaa.utils.block
import com.mafazaa.ainaa.utils.cancelWatchdog
import com.mafazaa.ainaa.utils.checkBlockedApp
import com.mafazaa.ainaa.utils.createNotification
import com.mafazaa.ainaa.utils.scheduleWatchdog
import com.mafazaa.ainaa.utils.shareFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
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
        when (intent?.action) {
            ACTION_START -> {
                MyLog.i(TAG, "Accessibility Service started and moved to foreground.")
                scheduleWatchdog()
            }
            ACTION_STOP -> {
                cancelWatchdog()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()

            }
            ACTION_SHARE_CURRENT_SCREEN -> {
                serviceScope.launch {
                    val screenAnalysis = ScreenAnalyser.analyzeScreen(
                        rootInActiveWindow, getString(R.string.app_name)
                    )
                    shareFile(logUiTree("screenShot", screenAnalysis))
                }
            }
            else -> {
                MyLog.w(TAG, "Unknown action received: ${intent?.action}")
            }
        }
        return START_STICKY
    }
    companion object {
        fun Context.startAccessibilityService(action: String = ACTION_START) {
            val intent = Intent(this, MyAccessibilityService::class.java).apply {
                this@apply.action = action
            }
            startService(intent)
        }

        const val ACTION_STOP = "STOP_ACCESSIBILITY"
        const val ACTION_START = "START_ACCESSIBILITY"
        const val ACTION_SHARE_CURRENT_SCREEN = "SHARE_CURRENT_SCREEN"
        internal const val NOTIFICATION_ID = 101 // Unique ID for the notification
        internal const val NOTIFICATION_CHANNEL_ID = "AINAA_PROTECTION_CHANNEL"
        internal const val WATCHDOG_INTERVAL_MS =  15 *60 * 1000L

        const val TAG = "MyAccessibilityService"

        private val SETTINGS_PACKAGE = DeviceUtils.settingsPackageName
        private val ACCESSIBILITY_SETTINGS = "${SETTINGS_PACKAGE}.accessibility.AccessibilitySettings"
    }

    override fun onCreate() {
        super.onCreate()
        MyLog.i(TAG, "Accessibility Service created.")
        startForeground(NOTIFICATION_ID, createNotification())
        scheduleWatchdog()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
        MyLog.i(TAG, "Accessibility Service connected.")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Return early if event is null or service is not running
        event ?: return
        // Only handle window content changed events
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
            return
        }
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val componentName = event.className?.toString()
            if (event.packageName == SETTINGS_PACKAGE &&
                (componentName == ACCESSIBILITY_SETTINGS ||
                        componentName?.contains("accessibility", true) == true)) {

                block(BlockReason.UsingBlockedApp(SETTINGS_PACKAGE))
                return
            }
        }

        rootInActiveWindow?.let { rootNode ->
            serviceScope.launch {
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
            }
        }
    }

    override fun onInterrupt() {
        serviceScope.cancel()
        MyLog.w(TAG, "Service interrupted")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        MyLog.w(TAG, "Task removed, scheduling restart")
        val restartIntent = Intent(this, RestartReceiver::class.java).apply {
            setPackage(packageName)
        }
        sendBroadcast(restartIntent)
        MyLog.d(TAG, "Restart broadcast sent")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        cancelWatchdog()

    }
}
