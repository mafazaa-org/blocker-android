package com.mafazaa.ainaa.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.mafazaa.ainaa.AppActivity

class AppDeviceAdminReceiver: DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        Toast.makeText(
            context,
            "Device Admin Disabled. Returning to App.",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(context, AppActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        super.onDisabled(context, intent)
    }
}