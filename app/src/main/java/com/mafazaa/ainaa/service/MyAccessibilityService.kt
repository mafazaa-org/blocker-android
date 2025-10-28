package com.mafazaa.ainaa.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.core.app.NotificationCompat
import com.mafazaa.ainaa.AppActivity
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
            ACTION_START -> {
                if (!isRunning) {
                    isRunning = true
                    startForeground(NOTIFICATION_ID, createNotification())
                    MyLog.i(TAG, "Accessibility Service started and moved to foreground.")
                }
            }
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
        private const val NOTIFICATION_ID = 101 // Unique ID for the notification
        private const val NOTIFICATION_CHANNEL_ID = "AINAA_PROTECTION_CHANNEL"
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
    private fun createNotification(): Notification {
        // Create a notification channel for Android 8.0 (API 26) and higher
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "حماية عائلة", // Channel name visible in settings
            NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
        ).apply {
            description = "الإشعار المستمر لتفعيل الحماية"
        }
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Create an intent that opens your app when the notification is tapped
        val pendingIntent = Intent(this, AppActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE)
        }

        // Build the notification
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("الحماية مفعلة")
            .setContentText("تطبيق عائلة يعمل على حمايتك في الخلفية.")
            .setSmallIcon(R.drawable.ic_red) // **Create a small, simple icon for this**
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()
    }

    // When the service is destroyed (e.g., by the system), try to restart it
    override fun onTaskRemoved(rootIntent: Intent?) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)
        // Use a PendingIntent to allow the system to restart it
        val restartServicePendingIntent = PendingIntent.getService(
            applicationContext, 1, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent)
        super.onTaskRemoved(rootIntent)
    }
}
