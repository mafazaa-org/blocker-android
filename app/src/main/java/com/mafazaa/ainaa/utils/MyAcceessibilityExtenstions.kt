package com.mafazaa.ainaa.utils

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.receiver.WatchdogReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
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



internal fun MyAccessibilityService.checkBlockedApp(currentApp: String?): Boolean {
    if (!this.isKeyguardSecure())
        return false
    return currentApp != null &&
            currentApp in sharedPrefs.blockedApps
}