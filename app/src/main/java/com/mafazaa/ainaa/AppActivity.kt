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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.setLayoutDirection
import androidx.lifecycle.lifecycleScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.helpers.LocaleHelper
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.receiver.AppDeviceAdminReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.ui.common.BottomBar
import com.mafazaa.ainaa.ui.common.OkDialog
import com.mafazaa.ainaa.ui.common.TopBar
import com.mafazaa.ainaa.ui.dialog.BlockAppDialog
import com.mafazaa.ainaa.ui.dialog.ConfirmBlockedDialog
import com.mafazaa.ainaa.ui.dialog.EnableProtectionDialog
import com.mafazaa.ainaa.ui.dialog.HowItWorksDialog
import com.mafazaa.ainaa.ui.dialog.PermissionDialog
import com.mafazaa.ainaa.ui.dialog.ReportProblemDialog
import com.mafazaa.ainaa.ui.protection.EnableProtectionScreen
import com.mafazaa.ainaa.ui.protection.ProtectionActivatedScreen
import com.mafazaa.ainaa.ui.support.SupportScreen
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.utils.Constants.JOIN_URL
import com.mafazaa.ainaa.utils.Constants.SAFE_SEARCH_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_CONTACT_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_URL
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.getAllApps
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.hasAdminPermission
import com.mafazaa.ainaa.utils.hasNotificationPermission
import com.mafazaa.ainaa.utils.hasOverlayPermission
import com.mafazaa.ainaa.utils.hasUsageStatsPermission
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.installApk
import com.mafazaa.ainaa.utils.openUrl
import com.mafazaa.ainaa.utils.requestAccessibilityPermission
import com.mafazaa.ainaa.utils.requestAdminPermission
import com.mafazaa.ainaa.utils.requestDrawOverlaysPermission
import com.mafazaa.ainaa.utils.requestVpnPermission
import com.mafazaa.ainaa.utils.shareFile
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

    val backStack = mutableStateListOf(
        if (!MyAccessibilityService.isRunning) Screen.EnableProtection
        else Screen.ProtectionActivated
    )
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
                    )
                }
            }
        }

    }

    @Composable
    private fun MainRoot(
        context: Context = LocalContext.current,
        viewModel: AppViewModel,
        sharedPrefs: SharedPrefs,
    ) {
        val snackbarHostState = remember { SnackbarHostState() }
        val apps = viewModel.apps.collectAsState().value

        // Centralized dialogs rendering
        when (val d = dialogState) {
            is DialogState.ReportProblem -> {
                ReportProblemDialog(
                    onClose = { dialogState = null },
                    onSubmit = { report ->
                        viewModel.submitReport(report) {
                            when (it) {
                                NetworkResult.Success -> {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.report_sent_message),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                is NetworkResult.Error -> {
                                    Toast.makeText(
                                        context,
                                        getString(R.string.report_send__faild_message, '$'),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                else -> {}
                            }
                        }
                        dialogState = null
                    }
                )
            }

            is DialogState.FirstTime -> {
                OkDialog(
                    title = stringResource(R.string.test_version_text),
                    message = stringResource(R.string.test_version_message).trimIndent(),
                    onDismiss = {
                        dialogState = null
                        MyApp.isFirstTime = false
                    }
                )
            }

            is DialogState.Permission -> {
                PermissionDialog(
                    permissionState = d.permission,
                    onDismiss = { dialogState = null },
                    onClick = {
                        grantPermission(d.permission)
                        dialogState = null
                    }
                )
            }

            is DialogState.BlockApps -> {
                BlockAppDialog(
                    onDismiss = { dialogState = null },
                    appStates = apps,
                    onBlockClick = { app ->
                        dialogState = DialogState.BlockApps(confirmApp = app)
                    }
                )
                if (d.confirmApp != null) {
                    ConfirmBlockedDialog(
                        app = d.confirmApp,
                        onDismiss = { dialogState = DialogState.BlockApps() },
                        onConfirm = {
                            viewModel.toggleAppSelection(it.packageName)
                            dialogState = DialogState.BlockApps()
                        }
                    )
                }
            }

            is DialogState.HowItWorks -> {
                HowItWorksDialog(
                    onDismiss = { dialogState = null },
                    onContactClicked = { context.openUrl(SUPPORT_CONTACT_URL) },
                    onSafeSearchClicked = { context.openUrl(SAFE_SEARCH_URL) },
                    image = stringResource(R.string.howtoknow_asset).toUri()
                )
            }

            is DialogState.EnableProtectionConfirm -> {
                EnableProtectionDialog(
                    onConfirm = {
                        dialogState = null // Close the confirmation dialog
                        viewModel.saveLevel(d.level)

                        val nextPermission = findNextMissingPermission()
                        if (nextPermission == null) {
                            // All permissions are already granted! Activate protection.
                            Toast.makeText(
                                context,
                                context.getString(R.string.protection_activated_text),
                                Toast.LENGTH_LONG
                            ).show()
                            startAccessibilityService()
                            context.startVpnService()
                            backStack.add(Screen.ProtectionActivated)
                            backStack.remove(Screen.EnableProtection)
                        } else {
                            dialogState = DialogState.Permission(nextPermission)
                        }
                    },
                    onDismiss = { dialogState = null }
                )
            }

            null -> {}
        }

        // Current Screen
        val currentScreen = backStack.lastOrNull()

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopBar(
                    onBack = { backStack.removeLastOrNull() },
                    currentScreen = currentScreen,
                    supportUs = { backStack.add(Screen.Support) },
                    home = {
                        if (MyVpnService.isRunning) {
                            backStack.add(Screen.ProtectionActivated)
                        }
                    }
                )
            },
            bottomBar = {
                BottomBar(
                    modifier = Modifier,
                    appVersion = BuildConfig.VERSION_NAME,
                    androidVersion = Build.VERSION.RELEASE
                ) {
                    if (MyAccessibilityService.isRunning) {
                        backStack.add(Screen.ProtectionActivated)
                    }
                }
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface)
                .windowInsetsPadding(WindowInsets.systemBars)
        ) { innerPadding ->
            NavDisplay(
                modifier = Modifier.padding(innerPadding),
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                entryDecorators = listOf(
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator()
                ),
                entryProvider = { key ->
                    when (key) {
                        Screen.ProtectionActivated -> NavEntry(key) {
                            ProtectionActivatedScreen(
                                onSupportClick = { backStack.add(Screen.Support) },
                                onBlockAppClick = { dialogState = DialogState.BlockApps() },
                                onReportClick = { dialogState = DialogState.ReportProblem },
                                onConfirmProtectionClick = { dialogState = DialogState.HowItWorks },
                                onUpdateClick = { updateStatus ->
                                    when (updateStatus) {
                                        UpdateState.Downloaded -> {
                                            context.installApk(viewModel.updateFile())
                                        }

                                        is UpdateState.Failed,
                                        UpdateState.NoUpdate,
                                            -> {
                                            viewModel.handleUpdateStatus()
                                        }

                                        else -> {
                                        }
                                    }

                                },
                                updateState = viewModel.updateState.value,
                            )
                        }

                        Screen.Support -> NavEntry(key) {
                            SupportScreen(
                                onSupportClick = { openUrl(SUPPORT_URL) },
                                onJoinClick = { openUrl(JOIN_URL) },
                                onShareLogFile = { this@AppActivity.shareFile(viewModel.getLogFile()) },
                                onStopBlocking = { startAccessibilityService(MyAccessibilityService.ACTION_STOP) },
                                onOpenScreenShotWindow = {
                                    viewModel.showScreenshotOverlay(true)

                                }
                            )
                        }

                        Screen.EnableProtection -> NavEntry(key) {
                            var selectedLevel by remember { mutableStateOf(DnsProtectionLevel.LOW) }

                            EnableProtectionScreen(
                                report = { dialogState = DialogState.ReportProblem },
                                enableProtection = { level: DnsProtectionLevel ->
                                    selectedLevel = level
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
                                },
                                selectedLevel = selectedLevel,
                            )
                        }
                    }
                }
            )
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
            if (!MyAccessibilityService.isRunning) {
                Toast.makeText(
                    this,
                    getString(R.string.protection_activated_message)
                        .trimIndent(),
                    Toast.LENGTH_SHORT
                ).show()
                startAccessibilityService()
                startVpnService()
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
}