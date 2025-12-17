package com.mafazaa.ainaa

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.setLayoutDirection
import androidx.lifecycle.lifecycleScope
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.helpers.LocaleHelper
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.receiver.AppDeviceAdminReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.getAllApps
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.hasAdminPermission
import com.mafazaa.ainaa.utils.hasNotificationPermission
import com.mafazaa.ainaa.utils.hasOverlayPermission
import com.mafazaa.ainaa.utils.hasUsageStatsPermission
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.requestAccessibilityPermission
import com.mafazaa.ainaa.utils.requestAdminPermission
import com.mafazaa.ainaa.utils.requestDrawOverlaysPermission
import com.mafazaa.ainaa.utils.requestVpnPermission
import com.mafazaa.ainaa.utils.startVpnService
import com.mafazaa.ainaa.viewmodels.AppViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.getViewModel
import org.koin.java.KoinJavaComponent.inject

// Sealed dialog state to manage all dialogs from a single source of truth
sealed interface DialogState {
    data object ReportProblem : DialogState
    data object FirstTime : DialogState
    data class Permission(val permission: PermissionState) : DialogState

    // Keeps Block Apps dialog open, and optionally shows a nested confirm dialog for a selected app
    data class BlockApps(val confirmApp: AppInfo? = null) : DialogState
    data object HowItWorks : DialogState
    data class EnableProtectionConfirm(val level: DnsProtectionLevel) :
        DialogState
}


class AppActivity : ComponentActivity() {
    var dialogState by mutableStateOf<DialogState?>(if (MyApp.isFirstTime) DialogState.FirstTime else null)
    var selectedLevel by mutableStateOf(DnsProtectionLevel.NONE)
    val backStack by lazy {
        mutableStateListOf(
            if (!isServiceRunning(this, MyAccessibilityService::class.java)) Screen.EnableProtection
            else Screen.ProtectionActivated
        )
    }
    private var vpnPermission by mutableStateOf(false)
    private var overlayPermission by mutableStateOf(false)
    private var usageStatsPermission by mutableStateOf(false)
    private var accessibilityPermission by mutableStateOf(false)
    private var notificationPermission by mutableStateOf(
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    )
    private val adminReceiver by lazy {
        ComponentName(
            this,
            AppDeviceAdminReceiver::class.java
        )
    }
    private val requestAdmin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    {
        // Handle result if needed
    }
    private val permissionChain = listOf(
        PermissionState.Accessibility,
        PermissionState.Overlay,
        PermissionState.Administrative,
        PermissionState.Vpn,
        PermissionState.Notification,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setLayoutDirection(window.decorView, ViewCompat.LAYOUT_DIRECTION_RTL)
        val splashscreen = installSplashScreen()
        var keepSplashScreen = true
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(3000)
            keepSplashScreen = false
        }
        val viewModel: AppViewModel = getViewModel()
        val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
        viewModel.loadInstalledApps(getAllApps())
        MyLog.i(TAG, "Opening app")
        //viewModel.handleUpdateStatus(this)
        refreshPermissionState()
        enableEdgeToEdge()
        requestAdminPermission(adminReceiver, requestAdmin)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AinaaTheme {
                    MainRoot(
                        viewModel = viewModel,
                        sharedPrefs = sharedPrefs,
                        backStack = backStack,
                        dialogState = dialogState,
                        onDialogStateChange = { dialogState = it },
                        grantPermission = { permissionState ->
                            grantPermission(permissionState)
                        },
                        findNextMissingPermission =  ::findNextMissingPermission,
                        permissionDialogChecker = { PermissionDialogChecker(
                            selectedLevel = selectedLevel,
                            )
                        },
                        selectedLevel = selectedLevel,
                        onSelectedLevelChange = { selectedLevel = it }
                    )
                }
            }
        }

    }

    private fun refreshPermissionState() {
        if (!notificationPermission) {
            notificationPermission = hasNotificationPermission()
        }
        if (!vpnPermission) {
            vpnPermission = hasVpnPermission()
        }
        if (!overlayPermission) {
            overlayPermission = hasOverlayPermission()
        }
        if (!usageStatsPermission) {
            usageStatsPermission = hasUsageStatsPermission()
        }
        if (!accessibilityPermission) {
            accessibilityPermission = hasAccessibilityPermission()
        }

    }

    private fun findNextMissingPermission(): PermissionState? {
        // Check permissions in the defined order
        return permissionChain.firstOrNull { permission ->
            !when (permission) {
                PermissionState.Overlay -> hasOverlayPermission()
                PermissionState.Accessibility -> hasAccessibilityPermission()
                PermissionState.Administrative -> hasAdminPermission(adminReceiver)
                PermissionState.Vpn -> hasVpnPermission()
                PermissionState.Notification -> hasNotificationPermission()
                PermissionState.Granted -> true
            }
        }
    }

    private fun checkAndContinuePermissionLoop() {
        val nextPermission = findNextMissingPermission()
        if (nextPermission != null) {
            // If the user is still in the process and another permission is needed, show the next dialog.
            // We only show it if a permission dialog isn't already showing.
            if (dialogState == null) {
                dialogState = DialogState.Permission(nextPermission)
            }
        } else {
            // No more missing permissions!
            // Check if the service isn't running yet, then activate.
            if (!isServiceRunning(this, MyAccessibilityService::class.java)) {
                Toast.makeText(
                    this,
                    getString(R.string.protection_activated_message)
                        .trimIndent(),
                    Toast.LENGTH_SHORT
                ).show()
                startAccessibilityService(MyAccessibilityService.ACTION_START_FOREGROUND)
                startVpnService(MyAccessibilityService.ACTION_START_FOREGROUND)
                backStack.add(Screen.ProtectionActivated)
                backStack.remove(Screen.EnableProtection)
            }
        }
    }


    override fun onResume() {
        super.onResume()
        refreshPermissionState()
        checkAndContinuePermissionLoop()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        refreshPermissionState()
    }


    private fun grantPermission(permissionState: PermissionState) {
        when (permissionState) {
            PermissionState.Notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requestPermissions(arrayOf(POST_NOTIFICATIONS), 0)
                }
            }

            PermissionState.Vpn -> requestVpnPermission()
            PermissionState.Overlay -> requestDrawOverlaysPermission()
            PermissionState.Accessibility -> requestAccessibilityPermission()
            PermissionState.Administrative -> requestAdminPermission(adminReceiver, requestAdmin)
            PermissionState.Granted -> {}
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context) {
        val context = LocaleHelper.forceArabicLocale(newBase)
        super.attachBaseContext(context)
    }
    @Composable
    fun PermissionDialogChecker(
        selectedLevel: DnsProtectionLevel,
    ) {
        when {
            !vpnPermission -> dialogState =
                DialogState.Permission(PermissionState.Vpn)

            !notificationPermission -> dialogState =
                DialogState.Permission(PermissionState.Notification)

            !overlayPermission -> dialogState =
                DialogState.Permission(PermissionState.Overlay)

            !accessibilityPermission -> dialogState =
                DialogState.Permission(PermissionState.Accessibility)

            else -> dialogState = DialogState.EnableProtectionConfirm(
                level = selectedLevel,
            )
        }
    }
}