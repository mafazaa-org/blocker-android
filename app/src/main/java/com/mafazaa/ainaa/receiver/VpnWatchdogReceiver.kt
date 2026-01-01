package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.startVpnService

/**
 * VpnWatchdogReceiver monitors VPN service health and restarts it if needed
 */
class VpnWatchdogReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            context ?: return

            val action = intent?.action
            MyLog.d(TAG, "VPN Watchdog triggered by action: $action")

            // Check if VPN permission is granted
            if (!context.hasVpnPermission()) {
                MyLog.d(TAG, "VPN permission not granted, no action needed")
                return
            }

            // Check if service is running
            if (!isServiceRunning(context, MyVpnService::class.java)) {
                MyLog.w(TAG, "VPN Watchdog detects service is NOT running. Attempting to restart...")
                context.startVpnService()
            } else {
                MyLog.d(TAG, "VPN Watchdog confirms service is running. No action needed.")
            }

        } catch (e: Exception) {
            MyLog.e(TAG, "Error in VPN Watchdog: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "VpnWatchdogReceiver"
    }
}

