package com.mafazaa.ainaa.helpers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.utils.MyLog

object MyNotificationManager {
    const val SERVICE_CHANNEL_ID = "ainaa"
    const val UPDATE_CHANNEL_ID = "ainaa_update"
    const val SERVICE_ID = 1
    const val UPDATE_ID = 2
    var notificationChannelCreated = false
    private var accessibilityServiceStarted = false
    private var vpnServiceStarted = false
    private var accessibilityServiceInstance: MyAccessibilityService? = null
    private var vpnServiceInstance: MyVpnService? = null
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val serviceChannel = NotificationChannel(
            SERVICE_CHANNEL_ID,
            SERVICE_CHANNEL_ID,
            NotificationManager.IMPORTANCE_NONE
        )
        val updateChannel = NotificationChannel(
            UPDATE_CHANNEL_ID,
            UPDATE_CHANNEL_ID,
            NotificationManager.IMPORTANCE_HIGH
        )
        updateChannel.description = "تحديثات التطبيق"
        serviceChannel.description = "خدمة الحماية من عيناً سلسبيلا"
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(serviceChannel)
        manager?.createNotificationChannel(updateChannel)
        notificationChannelCreated = true
    }


    fun startForegroundService(service: Service) {
        if (!notificationChannelCreated) {
            createNotificationChannels(service)
        }

        // Track which service is starting and store instance
        when (service) {
            is MyAccessibilityService -> {
                accessibilityServiceStarted = true
                accessibilityServiceInstance = service
                MyLog.d("MyNotificationManager", "Accessibility service started. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
            is MyVpnService -> {
                vpnServiceStarted = true
                vpnServiceInstance = service
                MyLog.d("MyNotificationManager", "VPN service started. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
        }

        // Always start as foreground (Android requirement)
        showProtectionNotification(service)
    }

    // Update service state and notification without calling startForeground
    fun updateServiceState(service: Service) {
        when (service) {
            is MyAccessibilityService -> {
                accessibilityServiceStarted = true
                MyLog.d("MyNotificationManager", "Accessibility service updated. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
            is MyVpnService -> {
                vpnServiceStarted = true
                MyLog.d("MyNotificationManager", "VPN service updated. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
        }

        // If both services are now running, update the notification
        if (accessibilityServiceStarted && vpnServiceStarted) {
            updateBothNotifications(service.applicationContext)
        }
    }

    private fun updateBothNotifications(context: Context) {
        MyLog.d("MyNotificationManager", "Both services running, updating notification")

        // Use one of the running services to update the foreground notification
        val serviceToUpdate = vpnServiceInstance ?: accessibilityServiceInstance

        if (serviceToUpdate != null) {
            val channelId = SERVICE_CHANNEL_ID
            val notification = Notification.Builder(serviceToUpdate, channelId)
                .setContentTitle("عينا سلسبيلا")
                .setContentText("الحماية مفعلة")
                .setSmallIcon(R.drawable.ic_auto_protect)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

            // Update the foreground notification
            serviceToUpdate.startForeground(SERVICE_ID, notification)
            MyLog.d("MyNotificationManager", "Notification updated to 'الحماية مفعلة'")
        } else {
            MyLog.w("MyNotificationManager", "No service instance available to update notification")
        }
    }

    fun stopForegroundService(service: Service) {
        // Track which service is stopping and clear instance
        when (service) {
            is MyAccessibilityService -> {
                accessibilityServiceStarted = false
                accessibilityServiceInstance = null
                MyLog.d("MyNotificationManager", "Accessibility service stopped. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
            is MyVpnService -> {
                vpnServiceStarted = false
                vpnServiceInstance = null
                MyLog.d("MyNotificationManager", "VPN service stopped. VPN: $vpnServiceStarted, Accessibility: $accessibilityServiceStarted")
            }
        }

        // Always stop foreground when service stops
        service.stopForeground(Service.STOP_FOREGROUND_REMOVE)
    }

    private fun showProtectionNotification(service: Service) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channelId = SERVICE_CHANNEL_ID

        // Show different content based on whether both services are running
        val title = if (accessibilityServiceStarted && vpnServiceStarted) {
            "عينا سلسبيلا"
        } else {
            "عينا سلسبيلا"
        }

        val content = if (accessibilityServiceStarted && vpnServiceStarted) {
            "الحماية مفعلة"
        } else {
            "جاري تفعيل الحماية..."
        }

        MyLog.d("MyNotificationManager", "Showing notification: $title - $content (VPN: $vpnServiceStarted, Acc: $accessibilityServiceStarted)")

        val notification = Notification.Builder(service, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_auto_protect)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
        service.startForeground(SERVICE_ID, notification)
    }

    fun showUpdateNotification(context: Context) {
        if (!notificationChannelCreated) {
            createNotificationChannels(context)
        }
        val notification = NotificationCompat.Builder(context, UPDATE_CHANNEL_ID)
            .setContentTitle("إصدار جديد متوفر")
            .setContentText("هناك إصدار جديد من تطبيق عيناً متوفر، يرجى التحديث إلى آخر إصدار لتحسين تجربتك.")
            .setSmallIcon(R.drawable.ic_red)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(UPDATE_ID, notification)
    }

    /**
     * Sync notification state based on currently running services.
     * Call this when the app starts to ensure notification reflects actual service state.
     */
    fun syncNotificationState(context: Context, accessibilityRunning: Boolean, vpnRunning: Boolean) {
        MyLog.d("MyNotificationManager", "Syncing notification state - Accessibility: $accessibilityRunning, VPN: $vpnRunning")

        accessibilityServiceStarted = accessibilityRunning
        vpnServiceStarted = vpnRunning

        // If both services are running, try to update the notification
        if (accessibilityRunning && vpnRunning) {
            MyLog.d("MyNotificationManager", "Both services already running, attempting to update notification")

            // Try to use stored service instance if available
            val serviceToUpdate = vpnServiceInstance ?: accessibilityServiceInstance

            if (serviceToUpdate != null) {
                MyLog.d("MyNotificationManager", "Service instance available, updating via startForeground")
                val channelId = SERVICE_CHANNEL_ID
                val notification = Notification.Builder(serviceToUpdate, channelId)
                    .setContentTitle("عينا سلسبيلا")
                    .setContentText("الحماية مفعلة")
                    .setSmallIcon(R.drawable.ic_auto_protect)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setCategory(NotificationCompat.CATEGORY_SERVICE)
                    .build()

                serviceToUpdate.startForeground(SERVICE_ID, notification)
                MyLog.d("MyNotificationManager", "Notification updated via service to 'الحماية مفعلة'")
            } else {
                MyLog.d("MyNotificationManager", "No service instance stored yet. Services will update notification when they call startForegroundService()")
                // Note: The notification will be properly set when services call startForegroundService()
                // This can happen on next service restart or when updateServiceState() is called
            }
        }
    }
}