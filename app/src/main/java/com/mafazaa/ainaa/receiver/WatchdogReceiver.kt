package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.utils.MyLog


/**
 * WatchdogReceiver listens for watchdog broadcasts to ensure the accessibility service is running
 * even if the system kills it in forced background scenarios.
 * When a broadcast is received, it checks if the accessibility service is active and restarts it if necessary.
 * This helps maintain the app's functionality by keeping the accessibility service operational.
 *  @see MyAccessibilityService for the service being monitored.
 */
class WatchdogReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startAccessibilityService()
        MyLog.d(
            MyAccessibilityService.Companion.TAG,
            "Watchdog tick -> service ensure."
        )
    }

    companion object {
        private const val TAG = "WatchdogReceiver"
    }
}

