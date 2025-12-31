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
import com.mafazaa.ainaa.domain.models.ScreenAnalysis
import com.mafazaa.ainaa.domain.models.ScreenNode
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.helpers.LockOverlayManager
import com.mafazaa.ainaa.helpers.ScreenAnalyser
import com.mafazaa.ainaa.utils.Constants.browserPackages
import com.mafazaa.ainaa.utils.Constants.socialMediaPackages
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.MyLog.logUiTree
import com.mafazaa.ainaa.utils.createNotification
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
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {

                startForeground(
                    NOTIFICATION_ID,
                    createNotification()
                )
                MyLog.i(TAG, "Accessibility Service started in foreground.")
                isRunning = true
            }

            ACTION_START_FOREGROUND -> {

                MyLog.i(TAG, "Accessibility Service started and moved to foreground.")
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
                    rootInActiveWindow?.let {
                        val screenAnalysis = ScreenAnalyser.analyzeScreen(
                            it, getString(R.string.app_name)
                        )
                        shareFile(logUiTree("screenShot", screenAnalysis))
                    }
                }
            }

            else -> {
                isRunning = true
                MyLog.w(TAG, "Unknown action received: ${intent?.action}")
            }
        }
        return START_STICKY
    }

    companion object {
        fun Context.startAccessibilityService(action: String = ACTION_START_FOREGROUND) {
            val intent = Intent(this, MyAccessibilityService::class.java).apply {
                this@apply.action = action
            }
            startService(intent)
        }

        const val ACTION_STOP = "STOP_ACCESSIBILITY"
        var isRunning = false

        const val ACTION_START_FOREGROUND = "START_ACCESSIBILITY_FOREGROUND"
        const val ACTION_SHARE_CURRENT_SCREEN = "SHARE_CURRENT_SCREEN"
        internal const val NOTIFICATION_ID = 101 // Unique ID for the notification
        internal const val NOTIFICATION_CHANNEL_ID = "AINAA_PROTECTION_CHANNEL"

        const val TAG = "MyAccessibilityService"

    }

    override fun onCreate() {
        super.onCreate()
        MyLog.i(TAG, "Accessibility Service created.")
        //scheduleWatchdog()
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
            }
        }
    }

    fun MyAccessibilityService.block(reason: BlockReason) {
        this.serviceScope.launch(Dispatchers.Main) {
            lockOverlayManager.showOverlay(reason)
        }
        this.performGlobalAction(GLOBAL_ACTION_BACK)
    }


    fun MyAccessibilityService.checkBlockedApp(currentApp: String?): Boolean {
        if (!this.isKeyguardSecure())//this means the phone is locked and the local data is encrypted
            return false
        return currentApp != null &&
                currentApp in sharedPrefs.blockedApps
    }

    suspend fun MyAccessibilityService.checkBlockedWords(screenAnalysis: ScreenAnalysis): BlockReason.BlockedWordDetected? {
        val maxNodes = 500
        if (!this.isKeyguardSecure())//this means the phone is locked and the local data is encrypted
            return null
        if (sharedPrefs.blockedWords.isEmpty()) {
            return null
        }
        return withContext(Dispatchers.Default) {
            for (word in sharedPrefs.blockedWords) {
                val stack = emptyList<ScreenNode>().toMutableList()
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
    }

    override fun onInterrupt() {
        serviceScope.cancel()
        MyLog.w(TAG, "Service interrupted")

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        MyLog.w(TAG, "Task removed, scheduling restart")
        MyLog.d(TAG, "Restart broadcast sent")
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

    }
}
