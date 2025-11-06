package com.mafazaa.ainaa.utils

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.mafazaa.ainaa.AppActivity
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.receiver.WatchdogReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.NOTIFICATION_CHANNEL_ID
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.TAG
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.WATCHDOG_INTERVAL_MS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal fun MyAccessibilityService.scheduleWatchdog() {
    this.alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

    val intent = Intent(this, WatchdogReceiver::class.java)
    this.watchdogPendingIntent = PendingIntent.getBroadcast(
        this,
        0, // request code
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    this.watchdogPendingIntent?.let {
        alarmManager?.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + WATCHDOG_INTERVAL_MS,
            WATCHDOG_INTERVAL_MS,
            it
        )
        MyLog.d(TAG, "Watchdog alarm scheduled.")
    }
}

internal fun MyAccessibilityService.cancelWatchdog() {
    this.watchdogPendingIntent?.let {
        this.alarmManager?.cancel(it)
        MyLog.d(TAG, "Watchdog alarm cancelled.")
    }
}

internal fun MyAccessibilityService.block(reason: BlockReason) {
    this.serviceScope.launch(Dispatchers.Main) {
        lockOverlayManager.showOverlay(reason)
    }
    this.performGlobalAction(GLOBAL_ACTION_BACK)
}

internal fun MyAccessibilityService.createNotification(): Notification {
    val channel = NotificationChannel(
        NOTIFICATION_CHANNEL_ID,
        "حماية عائلة", // Channel name visible in settings
        NotificationManager.IMPORTANCE_LOW // Low importance to be less intrusive
    ).apply {
        description = "الإشعار المستمر لتفعيل الحماية"
    }
    val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
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

internal fun MyAccessibilityService.checkBlockedApp(currentApp: String?): Boolean {
    if (!this.isKeyguardSecure())
        return false
    return currentApp != null &&
            currentApp in sharedPrefs.blockedApps
}