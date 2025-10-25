package com.mafazaa.ainaa

import android.content.Context
import android.os.Build
import android.widget.Toast
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.navigation.Screen
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
import com.mafazaa.ainaa.utils.Constants.JOIN_URL
import com.mafazaa.ainaa.utils.Constants.SAFE_SEARCH_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_CONTACT_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_URL
import com.mafazaa.ainaa.utils.installApk
import com.mafazaa.ainaa.utils.openUrl
import com.mafazaa.ainaa.utils.shareFile
import com.mafazaa.ainaa.utils.startVpnService
import com.mafazaa.ainaa.viewmodels.AppViewModel

@Composable
private fun MainRoot(
    viewModel: AppViewModel,
    sharedPrefs: SharedPrefs,
    grantPermission: (PermissionState) -> Unit,
    vpnPermission: Boolean,
    notificationPermission: Boolean,
    overlayPermission: Boolean,
    accessibilityPermission: Boolean,
    startAccessibilityService : (String) -> Unit = {},
    p1: () -> Unit = {},
    openUrl : (String) -> Unit,
    context: Context = LocalContext.current,

    ) {
    val snackbarHostState = remember { SnackbarHostState() }
    val backStack = remember {
        mutableStateListOf(
            if (!MyAccessibilityService.isRunning) Screen.EnableProtection
            else Screen.ProtectionActivated
        )
    }
    val context = LocalContext.current
    val apps = viewModel.apps.collectAsState().value

    // Single dialog state drives all dialogs; initialize with FirstTime if needed
    var dialogState by remember {
        mutableStateOf<DialogState?>(if (MyApp.isFirstTime) DialogState.FirstTime else null)
    }

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
                                    "فشل في أرسال التقرير : ${'$'}{it.message}",
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
                image = "file:///android_asset/howToKnow.jpg".toUri()
            )
        }

        is DialogState.EnableProtectionConfirm -> {
            EnableProtectionDialog(
                onConfirm = {
                    // Submit phone & activate

                    Toast.makeText(
                        context,
                        context.getString(R.string.protection_activated_text),
                        Toast.LENGTH_LONG
                    ).show()
                    viewModel.saveLevel(d.level)
                    startAccessibilityService( MyAccessibilityService.ACTION_START )
                    context.startVpnService()
                    backStack.add(Screen.ProtectionActivated)
                    backStack.remove(Screen.EnableProtection)
                    dialogState = null

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
                            onShareLogFile = { context.shareFile(viewModel.getLogFile()) },
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