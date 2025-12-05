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
import androidx.core.content.edit

private const val PREF_WATCHDOG = "watchdog_pref"
private const val KEY_INTERVAL = "interval_ms"
private const val KEY_STABLE = "stable_hits"
private const val BASE_INTERVAL_MS = 5 * 60 * 1000L         // 5 min
private const val MAX_INTERVAL_MS = 30 * 60 * 1000L         // 30 min
private const val STABLE_THRESHOLD = 5
internal fun MyAccessibilityService.scheduleWatchdog() {alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val prefs = getSharedPreferences(PREF_WATCHDOG, Context.MODE_PRIVATE)
    var interval = prefs.getLong(KEY_INTERVAL, BASE_INTERVAL_MS)
    val stableHits = prefs.getInt(KEY_STABLE, 0)

    // Increase interval after enough stable cycles (capped)
    if (stableHits >= STABLE_THRESHOLD && interval < MAX_INTERVAL_MS) {
        interval = (interval * 2).coerceAtMost(MAX_INTERVAL_MS)
        prefs.edit {
            putLong(KEY_INTERVAL, interval)
                .putInt(KEY_STABLE, 0)
        }
        MyLog.d(TAG, "Watchdog interval backed off to $interval ms.")
    }

    val intent = Intent(this, WatchdogReceiver::class.java)
    watchdogPendingIntent = PendingIntent.getBroadcast(
        this,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    watchdogPendingIntent?.let {
        val triggerAt = SystemClock.elapsedRealtime() + interval
        val flexWindow = interval / 4 // allow batching
        // Non-wakeup variant avoids turning screen/device on
        alarmManager?.setWindow(
            AlarmManager.ELAPSED_REALTIME,
            triggerAt,
            flexWindow,
            it
        )
        MyLog.d(TAG, "Adaptive watchdog scheduled interval=$interval flex=$flexWindow.")
    }
}

internal fun MyAccessibilityService.cancelWatchdog() {
    watchdogPendingIntent?.let {
        alarmManager?.cancel(it)
        MyLog.d(TAG, "Watchdog cancelled.")
    }
}

internal fun MyAccessibilityService.noteWatchdogStable() {
    val prefs = getSharedPreferences(PREF_WATCHDOG, Context.MODE_PRIVATE)
    val stableHits = prefs.getInt(KEY_STABLE, 0) + 1
    prefs.edit { putInt(KEY_STABLE, stableHits) }
    MyLog.d(TAG, "Watchdog stable hits=$stableHits")
}

internal fun MyAccessibilityService.resetWatchdogInterval() {
    getSharedPreferences(PREF_WATCHDOG, Context.MODE_PRIVATE)
        .edit {
            putLong(KEY_INTERVAL, BASE_INTERVAL_MS)
                .putInt(KEY_STABLE, 0)
        }
    MyLog.d(TAG, "Watchdog interval reset to base.")
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
    val restartIntent = Intent(this, MyAccessibilityService::class.java).apply { action = ACTION_START }
    val pendingIntent = PendingIntent.getService(
        this,
        1,
        restartIntent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val triggerAt = SystemClock.elapsedRealtime() + 2000
    // Non-wakeup: assume device likely active; avoids extra wake
    alarmManager.set(AlarmManager.ELAPSED_REALTIME, triggerAt, pendingIntent)
    MyLog.d(TAG, "Restart alarm (2s) scheduled (non-wakeup).")
}

internal fun MyAccessibilityService.cancelRestartAlarm() {
    val restartIntent = Intent(this, MyAccessibilityService::class.java).apply { action = ACTION_START }
    val pendingIntent = PendingIntent.getService(
        this,
        1,
        restartIntent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        MyLog.d(TAG, "Restart alarm cancelled.")
    }
}