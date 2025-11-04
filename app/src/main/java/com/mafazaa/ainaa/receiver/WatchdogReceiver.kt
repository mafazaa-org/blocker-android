package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.utils.MyLog


/**
 * WatchdogReceiver listens for watchdog broadcasts to ensure the accessibility service is running
 * even if the system kills it in forced background scenarios.
 * When a broadcast is received, it checks if the accessibility service is active and restarts it if necessary.
 * This helps maintain the app's functionality by keeping the accessibility service operational.
 *  @see MyAccessibilityService for the service being monitored.
 */
class WatchdogReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == null) {
            MyLog.w(TAG, "Received broadcast with null action. Ignoring.")
            return
        }
        MyLog.d(TAG, "Received watchdog broadcast")

        if (!MyAccessibilityService.isRunning) {
            MyLog.d(TAG, "Accessibility service is not running. Restarting...")
            val restartIntent = Intent(context, MyAccessibilityService::class.java).apply {
                this.action = MyAccessibilityService.ACTION_START
            }
            context?.startService(restartIntent)
        } else {
            MyLog.d(TAG, "Accessibility service is already running.")
        }
    }

    companion object {
        private const val TAG = "WatchdogReceiver"
    }
}

