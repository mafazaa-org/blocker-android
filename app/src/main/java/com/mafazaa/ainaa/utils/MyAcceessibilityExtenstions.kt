package com.mafazaa.ainaa.utils

import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.SystemClock
import com.mafazaa.ainaa.domain.models.BlockReason
import com.mafazaa.ainaa.receiver.WatchdogReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.ACTION_START
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

internal fun MyAccessibilityService.scheduleRestart() {
    val restartIntent = Intent(this, MyAccessibilityService::class.java).apply {
        action = ACTION_START
    }

    val pendingIntent = PendingIntent.getService(
        this,
        1, // A unique request code
        restartIntent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
    // Schedule the alarm to fire after a 2-second delay
    val triggerAtMillis = SystemClock.elapsedRealtime() + 2000
    alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
    MyLog.d(TAG, "Delayed restart alarm scheduled.")
}

internal fun MyAccessibilityService.cancelRestartAlarm() {
    val restartIntent = Intent(this, MyAccessibilityService::class.java).apply {
        action = ACTION_START
    }
    val pendingIntent = PendingIntent.getService(
        this,
        1,
        restartIntent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )

    if (pendingIntent != null) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        MyLog.d(TAG, "Restart alarm cancelled.")
    }
}