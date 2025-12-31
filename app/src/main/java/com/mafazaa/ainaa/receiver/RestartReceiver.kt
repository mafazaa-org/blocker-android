package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.service.MyAccessibilityService

import com.mafazaa.ainaa.utils.MyLog

/**
 * Receiver to restart the accessibility service when needed (e.g., after being killed).
 */
class RestartReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            MyLog.d(TAG, "Received restart broadcast")
            val serviceIntent = Intent(context, MyAccessibilityService::class.java).apply {
                this.action = MyAccessibilityService.ACTION_START_FOREGROUND
            }
            context?.let {
                it.startForegroundService(serviceIntent)
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in RestartReceiver: ${e.message}", e)
        }
    }
    companion object {
        private const val TAG = "RestartReceiver"
    }
}