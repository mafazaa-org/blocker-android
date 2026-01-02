package com.mafazaa.ainaa.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.SystemClock
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
    private var alarmManager: AlarmManager? = null
    private var watchdogPendingIntent: PendingIntent? = null

    companion object {
        internal const val TAG = "MyVpnService"
        const val ACTION_START = "START_VPN"
        const val ACTION_STOP = "STOP_VPN"
        var isRunning = false//todo remove
        private const val WATCHDOG_REQUEST_CODE = 2002
        private const val WATCHDOG_INTERVAL = 5 * 60 * 1000L // 5 minutes
    }

    override fun onCreate() {
        try {
            super.onCreate()
            VpnMonitorJobService.scheduleJob(this)
            scheduleWatchdog()
            MyLog.i(TAG, "VPN service created, monitor job and watchdog scheduled")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onCreate: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return try {
            when (intent?.action) {
                ACTION_STOP -> {
                    stopVpn()
                    START_NOT_STICKY
                }

                else -> {
                    // Start foreground immediately to satisfy Android requirements
                    MyNotificationManager.startForegroundService(this)

                    val level = sharedPrefs.dnsProtectionLevel
                    startVpn(level)
                    START_STICKY
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onStartCommand: ${e.message}", e)
            START_STICKY
        }
    }


    private fun startVpn(dnsProtectionLevel: DnsProtectionLevel) {
        try {
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
            MyLog.d(TAG, "VPN interface established successfully")

            // Notify that VPN is now fully operational
            MyNotificationManager.updateServiceState(this)
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in startVpn: ${e.message}", e)
            stopSelf()
        }
    }

    private fun stopVpn() {
        try {
            MyLog.d(TAG, "Stopping VPN service")
            vpnInterface?.close()
            vpnInterface = null
            isRunning = false
            MyNotificationManager.stopForegroundService(this)
            stopSelf()
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in stopVpn: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        try {
            MyLog.w(TAG, "VPN service being destroyed, scheduling restart")
            isRunning = false

            // Notify that VPN service is stopping
            MyNotificationManager.stopForegroundService(this)

            // Send broadcast to restart VPN service if needed
            if (hasVpnPermission()) {
                val restartIntent = Intent(this, VpnRestartReceiver::class.java)
                restartIntent.action = VpnRestartReceiver.ACTION_RESTART_VPN
                sendBroadcast(restartIntent)
                MyLog.d(TAG, "VPN restart broadcast sent")

                // Reschedule watchdog
                scheduleWatchdog()
            }

            super.onDestroy()
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onDestroy: ${e.message}", e)
        }
    }

    private fun scheduleWatchdog() {
        try {
            if (alarmManager == null) {
                alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            }

            val intent = Intent(this, com.mafazaa.ainaa.receiver.VpnWatchdogReceiver::class.java)

            watchdogPendingIntent = PendingIntent.getBroadcast(
                this,
                WATCHDOG_REQUEST_CODE,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                } else {
                    PendingIntent.FLAG_UPDATE_CURRENT
                }
            )

            val triggerTime = SystemClock.elapsedRealtime() + WATCHDOG_INTERVAL

            // Use setExactAndAllowWhileIdle for better reliability
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    watchdogPendingIntent!!
                )
            } else {
                alarmManager?.setExact(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    triggerTime,
                    watchdogPendingIntent!!
                )
            }

            MyLog.d(TAG, "VPN watchdog alarm scheduled for ${WATCHDOG_INTERVAL / 1000} seconds")
        } catch (e: Exception) {
            MyLog.e(TAG, "Error scheduling VPN watchdog: ${e.message}", e)
        }
    }

    override fun onRevoke() {
        try {
            super.onRevoke()
            MyLog.w(TAG, "VPN permission revoked")
            stopVpn()

            // Schedule restart attempt after delay
            serviceScope.launch {
                try {
                    delay(5000) // Wait 5 seconds before attempting restart
                    if (hasVpnPermission()) {
                        MyLog.d(TAG, "VPN permission restored, attempting restart")
                        val restartIntent = Intent(this@MyVpnService, VpnRestartReceiver::class.java)
                        restartIntent.action = VpnRestartReceiver.ACTION_RESTART_VPN
                        sendBroadcast(restartIntent)
                    }
                } catch (e: Exception) {
                    MyLog.e(TAG, "Error attempting VPN restart after revoke: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in onRevoke: ${e.message}", e)
        }
    }
}