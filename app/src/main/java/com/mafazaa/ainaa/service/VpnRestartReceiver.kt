package com.mafazaa.ainaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.startVpnService

/**
 * VpnRestartReceiver listens for VPN restart broadcasts to automatically restart the VPN service
 * if it gets killed by the system or when the device boots.
 */
class VpnRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            MyLog.i(TAG, "VpnRestartReceiver triggered with action: ${intent?.action}")

            context?.let { ctx ->
                when (intent?.action) {
                    ACTION_RESTART_VPN -> {
                        if (ctx.hasVpnPermission()) {
                            MyLog.i(TAG, "Attempting to restart VPN Service")
                            ctx.startVpnService()
                        } else {
                            MyLog.w(TAG, "VPN permission not granted, cannot restart")
                        }
                    }
                    Intent.ACTION_BOOT_COMPLETED -> {
                        if (ctx.hasVpnPermission()) {
                            MyLog.i(TAG, "Device booted, restarting VPN Service")
                            ctx.startVpnService()
                        }
                    }
                    Intent.ACTION_MY_PACKAGE_REPLACED -> {
                        if (ctx.hasVpnPermission()) {
                            MyLog.i(TAG, "App updated, restarting VPN Service")
                            ctx.startVpnService()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in VpnRestartReceiver: ${e.message}", e)
        }
    }

    companion object {
        const val TAG = "VpnRestartReceiver"
        const val ACTION_RESTART_VPN = "com.mafazaa.ainaa.RESTART_VPN"
    }
}

