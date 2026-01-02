package com.mafazaa.ainaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.startVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * VpnRestartReceiver listens for VPN restart broadcasts to automatically restart the VPN service
 * if it gets killed by the system or when the device boots.
 */
class VpnRestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            MyLog.i(TAG, "VpnRestartReceiver triggered with action: ${intent?.action}")

            context?.let { ctx ->
                // Use goAsync() to extend the receiver's lifetime
                val pendingResult = goAsync()

                // Use a coroutine scope to handle async operations
                val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

                scope.launch {
                    try {
                        when (intent?.action) {
                            ACTION_RESTART_VPN -> {
                                if (ctx.hasVpnPermission()) {
                                    MyLog.i(TAG, "Attempting to restart VPN Service")
                                    restartVpnServiceSafely(ctx)
                                } else {
                                    MyLog.w(TAG, "VPN permission not granted, cannot restart")
                                }
                            }
                            Intent.ACTION_BOOT_COMPLETED -> {
                                if (ctx.hasVpnPermission()) {
                                    MyLog.i(TAG, "Device booted, restarting VPN Service")
                                    // Add delay after boot to ensure system is ready
                                    delay(2000)
                                    restartVpnServiceSafely(ctx)
                                }
                            }
                            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                                if (ctx.hasVpnPermission()) {
                                    MyLog.i(TAG, "App updated, restarting VPN Service")
                                    restartVpnServiceSafely(ctx)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        MyLog.e(TAG, "Error in async VPN restart: ${e.message}", e)
                    } finally {
                        // Finish the broadcast
                        pendingResult.finish()
                    }
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in VpnRestartReceiver: ${e.message}", e)
        }
    }

    /**
     * Safely restarts the VPN service with proper error handling
     * Uses a Handler with Main Looper to ensure proper context
     */
    private suspend fun restartVpnServiceSafely(context: Context) {
        try {
            // Small delay to ensure process is in good state
            delay(500)

            // Start service on Main thread with proper looper
            Handler(Looper.getMainLooper()).post {
                try {
                    context.startVpnService()
                    MyLog.i(TAG, "VPN Service restart initiated successfully")
                } catch (e: Exception) {
                    MyLog.e(TAG, "Failed to start VPN Service: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            MyLog.e(TAG, "Error in restartVpnServiceSafely: ${e.message}", e)
        }
    }

    companion object {
        const val TAG = "VpnRestartReceiver"
        const val ACTION_RESTART_VPN = "com.mafazaa.ainaa.RESTART_VPN"
    }
}

