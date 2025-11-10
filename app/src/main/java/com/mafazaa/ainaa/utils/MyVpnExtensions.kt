package com.mafazaa.ainaa.utils

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.Service.STOP_FOREGROUND_REMOVE
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.mafazaa.ainaa.AppActivity
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.service.MyVpnService.Companion.ACTION_START
import com.mafazaa.ainaa.service.MyVpnService.Companion.TAG


@SuppressLint("MissingPermission")
internal fun MyVpnService.scheduleRestart() {
    val restartIntent = Intent(this, MyVpnService::class.java).apply {
        action = ACTION_START
    }

    val pendingIntent = PendingIntent.getService(
        this,
        1, // Use a unique request code.
        restartIntent,
        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
    )

    val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
    // Schedule the alarm to fire after 2 seconds.
    val triggerAtMillis = SystemClock.elapsedRealtime() + 2000

    // Use setExactAndAllowWhileIdle for better reliability on modern Android.
    try {
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)

    } catch (e: SecurityException) {
        MyLog.e(TAG, "Failed to schedule restart alarm due to missing permission: ${e.message}", e)
    }

    MyLog.d(TAG, "Restart alarm scheduled.")
}

internal fun MyVpnService.cancelRestartAlarm() {
    val restartIntent = Intent(this, MyVpnService::class.java).apply {
        action = ACTION_START
    }
    val pendingIntent = PendingIntent.getService(
        this,
        1,
        restartIntent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
        MyLog.d(TAG, "Restart alarm canceled.")
    }
}
internal fun MyVpnService.stopVpn() {
    MyLog.d(TAG, "Stopping VPN service")
    cancelRestartAlarm()
    try {
        vpnInterface?.close()
    } catch (e: Exception) {
        MyLog.e(TAG, "Error closing VPN interface: ${e.message}", e)
    } finally {
        vpnInterface = null
    }
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
}

internal fun MyVpnService.startVpn(dnsProtectionLevel: DnsProtectionLevel) {
    val configureIntent = PendingIntent.getActivity(
        this,
        0,
        Intent(this, AppActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
    )

    val builder = Builder().apply {
        addAddress(Constants.VPN_ADDRESS, 32)
        addDnsServer(dnsProtectionLevel.primaryDns)
        addDnsServer(dnsProtectionLevel.secondaryDns)
        setSession(getString(R.string.app_name))
        setBlocking(true)
        setConfigureIntent(configureIntent)
        setMtu(1500)
    }

    MyLog.d("TAG", "Starting VPN service with protection level: $dnsProtectionLevel")

    vpnInterface?.close()
    vpnInterface = builder.establish()
    try {
        vpnInterface = builder.establish()
        if (vpnInterface == null) {
            MyLog.e(TAG, "Failed to establish VPN interface, user might have denied permission.")
            stopSelf() // Stop if we can't establish the VPN.
        } else {
            MyLog.d(TAG, "VPN interface established successfully.")
        }
    } catch (e: Exception) {
        MyLog.e(TAG, "Error establishing VPN: ${e.message}", e)
        stopSelf()
    }
}