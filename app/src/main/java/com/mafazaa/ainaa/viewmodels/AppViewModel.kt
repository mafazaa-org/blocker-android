package com.mafazaa.ainaa.viewmodels

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.DialogState
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.domain.repo.UpdateRepo
import com.mafazaa.ainaa.helpers.MyNotificationManager
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.hasAdminPermission
import com.mafazaa.ainaa.utils.hasNotificationPermission
import com.mafazaa.ainaa.utils.hasOverlayPermission
import com.mafazaa.ainaa.utils.hasUsageStatsPermission
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.startVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(
    private val context : Context,
    private val remoteRepo: RemoteRepo,
    private val sharedPrefs: SharedPrefs,
    private val fileRepo: FileRepo,
    private val updateRepo: UpdateRepo,
    private val screenshotOverlayManager: ScreenshotOverlayManager
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _blockedWords = MutableStateFlow<List<String>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()
    val blockedWords: StateFlow<List<String>> = _blockedWords.asStateFlow()

    var updateState = mutableStateOf<UpdateState>(UpdateState.NoUpdate)

     var vpnPermission by mutableStateOf(false)
     var overlayPermission by mutableStateOf(false)
     var usageStatsPermission by mutableStateOf(false)
     var accessibilityPermission by mutableStateOf(false)
     var adminPermission by mutableStateOf(false)
    var permissionState by mutableStateOf<PermissionState?>(null)
    private var notificationPermission by mutableStateOf(
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    )
    var showSupportSheet by  mutableStateOf(false)
    var showProtectionSheet by   mutableStateOf(false)

    var uninstallAppCheck by mutableStateOf(false)

    val backStack by lazy {
        mutableStateListOf(getInitialScreenState())
    }
    var supportAmount by mutableStateOf(1)
    var paymentMethod by mutableStateOf(0)

    var selectedLevel by mutableStateOf(DnsProtectionLevel.NONE)
    
    init {
        // check for current screen state

    }

    /**
     * Determines the initial screen state based on permissions and service status
     * @return Screen.ProtectionActivated if all permissions granted and services running, else Screen.EnableProtection
     */
    private fun getInitialScreenState(): Screen {
        // Refresh to get current permission state
        refreshPermissionState()

        // Check if all permissions are granted
        val allPermissionsGranted = permissionState == PermissionState.Granted

        // Check if both services are running
        val accessibilityRunning = isServiceRunning(context, MyAccessibilityService::class.java)
        val vpnRunning = isServiceRunning(context, MyVpnService::class.java)

        MyLog.d(TAG, "=== Initial Screen State Check ===")
        MyLog.d(TAG, "All Permissions Granted: $allPermissionsGranted")
        MyLog.d(TAG, "Accessibility Service Running: $accessibilityRunning")
        MyLog.d(TAG, "VPN Service Running: $vpnRunning")

        // Sync notification state based on currently running services
        MyNotificationManager.syncNotificationState(context, accessibilityRunning, vpnRunning)

        return if (allPermissionsGranted && accessibilityRunning && vpnRunning) {
            MyLog.i(TAG, "Starting app in PROTECTION ACTIVATED state")
            Screen.ProtectionActivated
        } else {
            MyLog.i(TAG, "Starting app in ENABLE PROTECTION state (missing permissions or services)")
            if (!allPermissionsGranted) {
                MyLog.w(TAG, "Reason: Not all permissions granted - $permissionState")
            }
            if (!accessibilityRunning) {
                MyLog.w(TAG, "Reason: Accessibility Service not running")
            }
            if (!vpnRunning) {
                MyLog.w(TAG, "Reason: VPN Service not running")
            }
            Screen.EnableProtection
        }
    }



    fun refreshPermissionState() {
        // ALWAYS check current permission status (not just when cached value is false)
        // This ensures we detect when permissions are revoked
        notificationPermission = context.hasNotificationPermission()
        vpnPermission = context.hasVpnPermission()
        overlayPermission = context.hasOverlayPermission()
        usageStatsPermission = context.hasUsageStatsPermission()
        accessibilityPermission = context.hasAccessibilityPermission()

        // Check admin permission if uninstall protection is enabled
        if (uninstallAppCheck) {
            val adminReceiver = android.content.ComponentName(
                context,
                com.mafazaa.ainaa.receiver.AppDeviceAdminReceiver::class.java
            )
            adminPermission = context.hasAdminPermission(adminReceiver)
        } else {
            // If uninstall protection is not enabled, consider admin permission as granted
            adminPermission = true
        }

        permissionState = when {
            !notificationPermission -> PermissionState.Notification
            !vpnPermission -> PermissionState.Vpn
            !overlayPermission -> PermissionState.Overlay
            !accessibilityPermission -> PermissionState.Accessibility
            !adminPermission -> PermissionState.Administrative
            else -> PermissionState.Granted
        }

    }


    fun loadInstalledApps(appList: List<AppInfo>) {
        val appList = appList.toMutableList()
        val selectedApps = sharedPrefs.blockedApps
        for (selectedApp in selectedApps) {
            val i = appList.indexOfFirst { selectedApp == it.packageName }
            if (i != -1) {
                appList[i] = appList[i].copy(isSelected = true)
            }
        }
        _apps.value = appList
    }

    fun loadBlockedWords() {
        _blockedWords.value = sharedPrefs.blockedWords.toList()
    }

    fun addBlockedWord(word: String) {
        if (word.isBlank()) return
        if (_blockedWords.value.contains(word)) return
        sharedPrefs.blockedWords = sharedPrefs.blockedWords.add(word)
        _blockedWords.value += word
    }

    fun removeBlockedWord(word: String) {
        if (!_blockedWords.value.contains(word)) return
        sharedPrefs.blockedWords = sharedPrefs.blockedWords.remove(word)
        _blockedWords.value -= word
    }

    fun showScreenshotOverlay(show: Boolean) {
        if (show) {
            screenshotOverlayManager.showOverlay()
        } else {
            screenshotOverlayManager.closeOverlay()
        }
    }

    fun handleUpdateStatus() {
        viewModelScope.launch {
            updateRepo.checkAndDownloadIfNeeded(BuildConfig.VERSION_CODE).collect {
                updateState.value = it
                Log.d(TAG, "Update state: $it")
            }
        }
    }

    fun toggleAppSelection(packageName: String) {
        if (sharedPrefs.blockedApps.firstOrNull { it == packageName } == null) {
            sharedPrefs.blockedApps = sharedPrefs.blockedApps.add(packageName)
        } else {
            sharedPrefs.blockedApps = sharedPrefs.blockedApps.remove(packageName)
        }
        _apps.value = _apps.value.map {
            if (it.packageName == packageName) it.copy(isSelected = !it.isSelected) else it
        }
    }



    fun submitReport(reportModel: ReportModel, result: (NetworkResult) -> Unit) {
        viewModelScope.launch {
            remoteRepo.submitReportToGoogleForm(reportModel)
                .collect { submitResult ->
                    MyLog.d(TAG, submitResult.toString())
                    result(submitResult)
                }
        }
    }

    fun getLogFile(): File {
        return fileRepo.getLogFile()
    }

    fun updateFile(): File {
        return fileRepo.getUpdateFile()
    }

    fun saveLevel(level: DnsProtectionLevel) {
        sharedPrefs.dnsProtectionLevel = level
    }

    /**
     * Checks if all required permissions are granted.
     * Called when user clicks "Enable Protection" button.
     *
     * @param onDialogStateChange Callback to show permission dialog if needed
     * @return True if all permissions are granted, false otherwise
     */
    fun checkAllPermissions(
        onDialogStateChange: (DialogState?) -> Unit
    ): Boolean {
        // Refresh permission state to get current status
        refreshPermissionState()

        Log.d(TAG, "=== Checking Permissions ===")
        Log.d(TAG, "Accessibility: $accessibilityPermission")
        Log.d(TAG, "VPN: $vpnPermission")
        Log.d(TAG, "Overlay: $overlayPermission")
        Log.d(TAG, "Permission State: $permissionState")

        if (permissionState != PermissionState.Granted) {
            Log.w(TAG, "Missing permission detected: $permissionState - showing dialog")
            // Not all permissions are granted, show permission dialog
            onDialogStateChange(DialogState.Permission(permissionState!!))
            return false
        }

        Log.i(TAG, "All permissions granted!")
        return true
    }

    /**
     * Activates protection by starting services and navigating to ProtectionActivated screen.
     * Should only be called after checkAllPermissions() returns true.
     *
     * @param backStack The navigation back stack to update
     */
    fun activateProtection(
        backStack: MutableList<Screen>
    ) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== Activating Protection ===")

                val accessibilityServiceRunning = isServiceRunning(context, MyAccessibilityService::class.java)
                val vpnServiceRunning = isServiceRunning(context, MyVpnService::class.java)

                Log.d(TAG, "Accessibility Service Running: $accessibilityServiceRunning")
                Log.d(TAG, "VPN Service Running: $vpnServiceRunning")

                // Start Accessibility Service if not running
                if (!accessibilityServiceRunning) {
                    Log.i(TAG, "Starting Accessibility Service...")
                    try {
                        // Start on Main thread for proper context
                        viewModelScope.launch(Dispatchers.Main) {
                            context.startAccessibilityService(MyAccessibilityService.ACTION_START_FOREGROUND)
                            Log.d(TAG, "Accessibility Service start initiated")
                        }.join()
                        // Wait for accessibility service to fully initialize
                        kotlinx.coroutines.delay(1500)
                        Log.d(TAG, "Accessibility Service initialized, ready to start VPN")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting Accessibility Service: ${e.message}", e)
                    }
                }

                // Start VPN Service if not running (with delay after accessibility)
                if (!vpnServiceRunning) {
                    Log.i(TAG, "Starting VPN Service...")
                    try {
                        // Start on Main thread for proper context
                        viewModelScope.launch(Dispatchers.Main) {
                            context.startVpnService(MyVpnService.ACTION_START)
                            Log.d(TAG, "VPN Service start initiated")
                        }.join()
                        // Wait for VPN service to fully initialize
                        kotlinx.coroutines.delay(1500)
                        Log.d(TAG, "VPN Service initialized")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error starting VPN Service: ${e.message}", e)
                        // Continue anyway, might be already running
                    }
                }

                // Wait longer for both services to fully start and initialize
                Log.d(TAG, "Waiting for all services to fully initialize...")
                kotlinx.coroutines.delay(1000)

                val accessibilityAfterStart = isServiceRunning(context, MyAccessibilityService::class.java)
                val vpnAfterStart = isServiceRunning(context, MyVpnService::class.java)

                Log.d(TAG, "After start - Accessibility: $accessibilityAfterStart, VPN: $vpnAfterStart")

                val servicesStarted = accessibilityAfterStart && vpnAfterStart

                if (servicesStarted) {
                    Log.i(TAG, "Services started successfully!")
                    // Services started successfully, navigate to ProtectionActivated screen
                    if (!backStack.contains(Screen.ProtectionActivated)) {
                        backStack.add(Screen.ProtectionActivated)
                    }
                    backStack.remove(Screen.EnableProtection)

                    // Show success message on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.protection_activated_text),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Log.e(TAG, "Services failed to start! Accessibility: $accessibilityAfterStart, VPN: $vpnAfterStart")
                    // Services failed to start, revert to EnableProtection screen
                    if (!backStack.contains(Screen.EnableProtection)) {
                        backStack.add(Screen.EnableProtection)
                    }
                    backStack.remove(Screen.ProtectionActivated)

                    // Show error message on main thread
                    viewModelScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            context,
                            "Failed to start protection services. Please check permissions.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in activateProtection", e)

                // Show error message on main thread
                viewModelScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "Error activating protection: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Revert to EnableProtection screen on error
                if (!backStack.contains(Screen.EnableProtection)) {
                    backStack.add(Screen.EnableProtection)
                }
                backStack.remove(Screen.ProtectionActivated)
            }
        }
    }

    /**
     * Combined function that checks permissions and activates protection.
     * Uses checkAllPermissions() and activateProtection() internally.
     * Kept for backward compatibility.
     *
     * @param backStack The navigation back stack to update
     * @param onDialogStateChange Callback to show permission dialog if needed
     */
    fun checkPermissionsAndActivateProtection(
        backStack: MutableList<Screen>,
        onDialogStateChange: (DialogState?) -> Unit
    ) {
        // Check permissions first
        if (checkAllPermissions(onDialogStateChange)) {
            // If all permissions granted, activate protection
            activateProtection(backStack)
        } else {
            // Permissions missing, ensure we're on EnableProtection screen
            if (!backStack.contains(Screen.EnableProtection)) {
                backStack.add(Screen.EnableProtection)
            }
            backStack.remove(Screen.ProtectionActivated)
        }
    }
}
