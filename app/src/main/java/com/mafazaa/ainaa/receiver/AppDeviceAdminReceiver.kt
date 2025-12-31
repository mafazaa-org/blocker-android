package com.mafazaa.ainaa.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.mafazaa.ainaa.AppActivity
import com.mafazaa.ainaa.utils.MyLog

class AppDeviceAdminReceiver: DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        try {
            super.onEnabled(context, intent)
            MyLog.i(TAG, "Device Admin enabled")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onEnabled: ${e.message}", e)
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        try {
            Toast.makeText(
                context,
                "Device Admin Disabled. Returning to App.",
                Toast.LENGTH_LONG
            ).show()
            val activityIntent = Intent(context, AppActivity::class.java)
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(activityIntent)
            super.onDisabled(context, intent)
            MyLog.i(TAG, "Device Admin disabled")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onDisabled: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "AppDeviceAdminReceiver"
    }
}