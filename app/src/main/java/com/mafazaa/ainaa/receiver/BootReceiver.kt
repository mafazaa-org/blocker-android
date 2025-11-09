package com.mafazaa.ainaa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.data.local.FakeFileRepo
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isKeyguardSecure
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.utils.startVpnService

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_REBOOT
        ) {
            MyLog.d("BootReceiver", "Device rebooted. Attempting to start accessibility service.")
            val serviceIntent = Intent(context, MyAccessibilityService::class.java).apply {
                action = MyAccessibilityService.ACTION_START
            }
            context.startForegroundService(serviceIntent)
        }

        if (!context.isKeyguardSecure()) {
            MyLog.fileRepo = FakeFileRepo
            MyLog.w(TAG, "Device is not encrypted, logging disabled")
        }
        MyLog.i(TAG, "Device :${intent.action}")
        if (context.hasAccessibilityPermission()) {
            context.startAccessibilityService()
        }
        if (context.hasVpnPermission()) {
            MyLog.i(TAG, "Starting vpn on boot")
            context.startVpnService()
        }

    }


    companion object {
        private const val TAG = "BootReceiver"
    }
}