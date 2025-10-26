package com.mafazaa.ainaa.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.MyLog.logUiTree
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.utils.isKeyguardSecure
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.helpers.ScreenAnalyser
import com.mafazaa.ainaa.domain.models.ScriptResult
import com.mafazaa.ainaa.domain.repo.ScriptRepo
import com.mafazaa.ainaa.helpers.LockOverlayManager
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
    lateinit var overlay: LockOverlayManager
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    lateinit var lockOverlayManager: LockOverlayManager

    private val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
    private val scriptRepo: ScriptRepo by inject(ScriptRepo::class.java)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                isRunning = false
                return START_NOT_STICKY
            }

            ACTION_SHARE_CURRENT_SCREEN -> {
                if (!isRunning) {
                    MyLog.w(TAG, "Service not running, cannot share screen")
                }
                serviceScope.launch {
                    val screenAnalysis = ScreenAnalyser.analyzeScreen(
                        rootInActiveWindow, getString(R.string.app_name)
                    )
                    shareFile(logUiTree("screenShot", screenAnalysis))
                }
            }

            else -> {//or ACTION_START
                isRunning = true
                return START_STICKY
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        lockOverlayManager = inject<LockOverlayManager>(LockOverlayManager::class.java).value
        overlay =
            LockOverlayManager(this) // This seems redundant as overlayManager is already initialized.



    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info
    }


    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Return early if event is null or service is not running
        event ?: return
        if (!isRunning) return
        // Only handle window content changed events
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

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
                
                // Check for blocked keywords
                val blockedKeyword = checkBlockedKeywords(analysisResult.root)
                if (blockedKeyword != null) {
                    MyLog.i(TAG, "Blocked keyword detected: $blockedKeyword")
                    block(BlockReason.UsingBlockedKeyword(blockedKeyword))
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


    private fun checkBlockedApp(currentApp: String?): Boolean {
        if (!this.isKeyguardSecure())
            return false
        return currentApp != null &&
                currentApp in sharedPrefs.blockedApps
    }

    private fun checkBlockedKeywords(screenNode: com.mafazaa.ainaa.domain.models.ScreenNode): String? {
        if (!this.isKeyguardSecure())
            return null
        return ScreenAnalyser.containsBlockedKeyword(screenNode, sharedPrefs.blockedKeywords)
    }

    private fun block(reason: BlockReason) {
        serviceScope.launch(Dispatchers.Main) {
            lockOverlayManager.showOverlay(reason)
        }
        performGlobalAction(GLOBAL_ACTION_BACK)
    }

    companion object {
        fun Context.startAccessibilityService(action: String = ACTION_START) {
            isRunning = true
            val intent = Intent(this, MyAccessibilityService::class.java).apply {
                this@apply.action = action
            }
            startService(intent)
        }

        const val ACTION_STOP = "STOP_ACCESSIBILITY"
        const val ACTION_START = "START_ACCESSIBILITY"
        const val ACTION_SHARE_CURRENT_SCREEN = "SHARE_CURRENT_SCREEN"
        var isRunning = false
        const val TAG = "MyAccessibilityService"
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        isRunning = false
    }

    override fun onInterrupt() {
        serviceScope.cancel()
        MyLog.w(TAG, "Service interrupted")
        isRunning = false

    }
}
