package com.mafazaa.ainaa.service

import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.mafazaa.ainaa.AppActivity
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.helpers.MyNotificationManager
import com.mafazaa.ainaa.utils.Constants
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasVpnPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class MyVpnService : VpnService() {
     var vpnInterface: ParcelFileDescriptor? = null
    private val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())

    companion object {
        internal const val TAG = "MyVpnService"
        const val ACTION_START = "START_VPN"
        const val ACTION_STOP = "STOP_VPN"
        var isRunning = false//todo remove
    }

    override fun onCreate() {
        super.onCreate()
        MyNotificationManager.startForegroundService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
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
    }


    private fun startVpn(dnsProtectionLevel: DnsProtectionLevel) {
        val emptyIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, AppActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = Builder().apply {
            addAddress(Constants.VPN_ADDRESS, 32)
            addDnsServer(dnsProtectionLevel.primaryDns)
            addDnsServer(dnsProtectionLevel.secondaryDns)
            setSession("SafeDNS")
            setBlocking(true)
            setConfigureIntent(emptyIntent) // منع إيقاف الخدمة من الإشعار
            setMtu(1500)
        }

        MyLog.d(TAG, "Starting VPN service with protection level: $dnsProtectionLevel")

        vpnInterface?.close()
        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            MyLog.e(TAG, "Failed to establish VPN interface")
            stopSelf()
            return
        }
        isRunning = true
    }

    private fun stopVpn() {
        MyLog.d(TAG, "Stopping VPN service")
        vpnInterface?.close()
        vpnInterface = null
        isRunning = false
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()//todo
        isRunning = false
        MyLog.d(TAG, "VPN service destroyed")
        serviceScope.launch {
            delay(5000)
            if (MyAccessibilityService.isRunning&&this@MyVpnService.hasVpnPermission()) {
                Log.d(TAG, "Restarting VPN service after revocation")
                this@MyVpnService.startVpn(sharedPrefs.dnsProtectionLevel)
            }

        }
    }

    override fun onRevoke() {
        super.onRevoke()//todo
        isRunning = false
        MyLog.d(TAG, "VPN revoked")
        serviceScope.launch {

        }
    }
}