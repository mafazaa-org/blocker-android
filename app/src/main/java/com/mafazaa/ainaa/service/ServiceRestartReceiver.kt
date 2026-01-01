package com.mafazaa.ainaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.utils.MyLog

class ServiceRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            MyLog.i(TAG, "ServiceRestartReceiver triggered with action: ${intent?.action}")

            when (intent?.action) {
                ACTION_RESTART_SERVICE,
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED -> {
                    context?.let {
                        MyLog.i(TAG, "Attempting to restart Accessibility Service")
                        it.startAccessibilityService()
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in ServiceRestartReceiver: ${e.message}", e)
        }
    }

    companion object {
        const val TAG = "ServiceRestartReceiver"
        const val ACTION_RESTART_SERVICE = "com.mafazaa.ainaa.RESTART_SERVICE"
    }
}

