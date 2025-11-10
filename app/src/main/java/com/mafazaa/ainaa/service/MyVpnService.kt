package com.mafazaa.ainaa.service

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.createNotification
import com.mafazaa.ainaa.utils.scheduleRestart
import com.mafazaa.ainaa.utils.startVpn
import com.mafazaa.ainaa.utils.stopVpn
import org.koin.java.KoinJavaComponent.inject

class MyVpnService : VpnService() {
    internal var vpnInterface: ParcelFileDescriptor? = null
    internal val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)

    companion object {
        internal const val TAG = "MyVpnService"
        const val ACTION_START = "START_VPN"

        const val ACTION_START_FOREGROUND = "START_VPN_FOREGROUND"
        const val ACTION_STOP = "STOP_VPN"
    }

    override fun onCreate() {
        super.onCreate()
        MyLog.d(TAG, "VPN service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_FOREGROUND -> {
                MyLog.d(TAG, "Moving VPN service to foreground.")
                startForeground(MyAccessibilityService.NOTIFICATION_ID, createNotification())
                val level = sharedPrefs.dnsProtectionLevel
                startVpn(level)
            }
            ACTION_STOP -> {
                stopVpn()
                return START_NOT_STICKY
            }

            else -> {
                val level = sharedPrefs.dnsProtectionLevel
                startVpn(level)
                return START_STICKY
            }
        }
        return  START_STICKY
    }

    override fun onDestroy() {
        MyLog.d(TAG, "VPN service destroyed")
        vpnInterface?.close()
        super.onDestroy()//todo

    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        MyLog.w(TAG, "Task removed by user. Scheduling a restart.")
        scheduleRestart()
        super.onTaskRemoved(rootIntent)
    }

    override fun onRevoke() {
        MyLog.w(TAG, "VPN permission revoked by user.")
        stopVpn()
        super.onRevoke()

    }

}