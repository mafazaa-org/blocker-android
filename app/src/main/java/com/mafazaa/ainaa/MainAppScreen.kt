package com.mafazaa.ainaa

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat.getString
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
import com.mafazaa.ainaa.ui.common.EnableProtectionBottomSheet
import com.mafazaa.ainaa.ui.common.MainDrawer
import com.mafazaa.ainaa.ui.common.OkDialog
import com.mafazaa.ainaa.ui.common.SupportUsBottomSheet
import com.mafazaa.ainaa.ui.common.TopBar
import com.mafazaa.ainaa.ui.dialog.BlockAppDialog
import com.mafazaa.ainaa.ui.dialog.ConfirmBlockedDialog
import com.mafazaa.ainaa.ui.dialog.EnableProtectionDialog
import com.mafazaa.ainaa.ui.dialog.HowItWorksDialog
import com.mafazaa.ainaa.ui.dialog.PermissionDialog
import com.mafazaa.ainaa.ui.dialog.ReportProblemDialog
import com.mafazaa.ainaa.ui.protection.EnableProtectionScreen
import com.mafazaa.ainaa.ui.protection.ProtectionActivatedScreen
import com.mafazaa.ainaa.utils.Constants.SAFE_SEARCH_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_CONTACT_URL
import com.mafazaa.ainaa.utils.installApk
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.openUrl
import com.mafazaa.ainaa.utils.startVpnService
import com.mafazaa.ainaa.viewmodels.AppViewModel
import kotlinx.coroutines.launch

/**
 * Checks if all required permissions are granted and activates protection accordingly.
 *
 * @param context The application context
 * @param viewModel The app view model
 * @param backStack The navigation back stack
 * @param findNextMissingPermission Function to find the next missing permission
 * @param onDialogStateChange Callback to change dialog state
 * @return True if all permissions are granted and services started successfully, false otherwise
 */
fun checkPermissionsAndActivateProtection(
    context: Context,
    viewModel: AppViewModel,
    backStack: MutableList<Screen>,
    findNextMissingPermission: () -> PermissionState,
    onDialogStateChange: (DialogState?) -> Unit
): Boolean {
    try {
        android.util.Log.d("PermissionCheck", "=== Starting Permission Check ===")

        // IMPORTANT: Refresh permission state before checking to get current status
        viewModel.refreshPermissionState()

        // Check if all permissions are granted
        val nextPermission = findNextMissingPermission()

        android.util.Log.d("PermissionCheck", "Next missing permission: $nextPermission")
        android.util.Log.d("PermissionCheck", "Accessibility: ${viewModel.accessibilityPermission}")
        android.util.Log.d("PermissionCheck", "VPN: ${viewModel.vpnPermission}")
        android.util.Log.d("PermissionCheck", "Overlay: ${viewModel.overlayPermission}")
        android.util.Log.d("PermissionCheck", "Permission State: ${viewModel.permissionState}")

        if (nextPermission != PermissionState.Granted) {
            android.util.Log.w("PermissionCheck", "Missing permission detected: $nextPermission - showing dialog")
            // Not all permissions are granted, show permission dialog
            onDialogStateChange(DialogState.Permission(nextPermission))

            // Ensure we're on EnableProtection screen
            if (!backStack.contains(Screen.EnableProtection)) {
                backStack.add(Screen.EnableProtection)
            }
            backStack.remove(Screen.ProtectionActivated)

            return false
        }

        // All permissions are granted, try to start services
        android.util.Log.i("PermissionCheck", "All permissions granted! Starting services...")

        val accessibilityServiceRunning = isServiceRunning(context, MyAccessibilityService::class.java)
        val vpnServiceRunning = isServiceRunning(context, MyVpnService::class.java)

        android.util.Log.d("PermissionCheck", "Accessibility Service Running: $accessibilityServiceRunning")
        android.util.Log.d("PermissionCheck", "VPN Service Running: $vpnServiceRunning")

        // Start services if not running
        if (!accessibilityServiceRunning) {
            android.util.Log.i("PermissionCheck", "Starting Accessibility Service...")
            context.startAccessibilityService(MyAccessibilityService.ACTION_START_FOREGROUND)
        }

        if (!vpnServiceRunning) {
            android.util.Log.i("PermissionCheck", "Starting VPN Service...")
            context.startVpnService(MyAccessibilityService.ACTION_START_FOREGROUND)
        }

        // Wait briefly and check if services started successfully
        Thread.sleep(500)

        val accessibilityAfterStart = isServiceRunning(context, MyAccessibilityService::class.java)
        val vpnAfterStart = isServiceRunning(context, MyVpnService::class.java)

        android.util.Log.d("PermissionCheck", "After start - Accessibility: $accessibilityAfterStart, VPN: $vpnAfterStart")

        val servicesStarted = accessibilityAfterStart && vpnAfterStart

        if (servicesStarted) {
            android.util.Log.i("PermissionCheck", "Services started successfully!")
            // Services started successfully, navigate to ProtectionActivated screen
            if (!backStack.contains(Screen.ProtectionActivated)) {
                backStack.add(Screen.ProtectionActivated)
            }
            backStack.remove(Screen.EnableProtection)

            Toast.makeText(
                context,
                context.getString(R.string.protection_activated_text),
                Toast.LENGTH_LONG
            ).show()

            return true
        } else {
            android.util.Log.e("PermissionCheck", "Services failed to start!")
            // Services failed to start, revert to EnableProtection screen
            if (!backStack.contains(Screen.EnableProtection)) {
                backStack.add(Screen.EnableProtection)
            }
            backStack.remove(Screen.ProtectionActivated)

            Toast.makeText(
                context,
                "Failed to start protection services",
                Toast.LENGTH_LONG
            ).show()

            return false
        }
    } catch (e: Exception) {
        android.util.Log.e("PermissionCheck", "Error in checkPermissionsAndActivateProtection", e)
        // Handle any errors
        Toast.makeText(
            context,
            "Error activating protection: ${e.message}",
            Toast.LENGTH_LONG
        ).show()

        // Revert to EnableProtection screen on error
        if (!backStack.contains(Screen.EnableProtection)) {
            backStack.add(Screen.EnableProtection)
        }
        backStack.remove(Screen.ProtectionActivated)

        return false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoot(
    context: Context = LocalContext.current,
    viewModel: AppViewModel,
    sharedPrefs: SharedPrefs,
    dialogState: DialogState?,
    onDialogStateChange: (DialogState?) -> Unit,
    grantPermission: (PermissionState) -> Unit,
    findNextMissingPermission: () -> PermissionState,
    permissionDialogChecker: @Composable () -> Unit,
    selectedLevel: DnsProtectionLevel = DnsProtectionLevel.NONE,
    onSelectedLevelChange: (DnsProtectionLevel) -> Unit = {}
) {

    val snackBarHostState = remember { SnackbarHostState() }
    val uiScope = rememberCoroutineScope()
    val apps = viewModel.apps.collectAsState().value
    val backStack = viewModel.backStack
    val currentScreen = backStack.lastOrNull()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState()
    permissionDialogChecker()

    // Current Screen
    MainDrawer(
        drawerState = drawerState,
        content = {
            Scaffold(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .windowInsetsPadding(WindowInsets.systemBars),
                snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
                topBar = {
                    TopBar(
                        onBack = { backStack.removeLastOrNull() },
                        currentScreen = currentScreen,
                        supportUs = {
                            onDialogStateChange(DialogState.BlockApps())
                        },
                        openMenu = {
                            uiScope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }
                    )
                },
                content = { innerPadding ->
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
                                        onSupportClick = {},
                                        onBlockAppClick = { onDialogStateChange(DialogState.BlockApps()) },
                                        onReportClick = { onDialogStateChange(DialogState.ReportProblem) },
                                        onConfirmProtectionClick = { onDialogStateChange(DialogState.HowItWorks) },
                                        onUpdateClick = { updateStatus -> when (updateStatus) {
                                                UpdateState.Downloaded -> {
                                                    context.installApk(viewModel.updateFile())
                                                }
                                                is UpdateState.Failed, UpdateState.NoUpdate -> {
                                                    viewModel.handleUpdateStatus()
                                                }
                                                else -> {

                                                }
                                            }
                                        },
                                        updateState = viewModel.updateState.value,
                                        onBlockWordClicked =  { onDialogStateChange(DialogState.HowItWorks) }
                                    )
                                }

                                Screen.Support -> NavEntry(key) {}

                                Screen.EnableProtection -> NavEntry(key) {
                                    EnableProtectionScreen(
                                        report = { onDialogStateChange(DialogState.ReportProblem) },
                                        enableProtection = { level: DnsProtectionLevel ->
                                            if (level == DnsProtectionLevel.NONE) {
                                                uiScope.launch {
                                                    snackBarHostState
                                                        .currentSnackbarData ?: snackBarHostState // Clear previous snackbar
                                                        .showSnackbar(
                                                            message = context.getString(R.string.pick_protect_level_text),
                                                            duration = SnackbarDuration.Short,
                                                        )
                                                }
                                                return@EnableProtectionScreen
                                            }

                                            onSelectedLevelChange(level)
                                            viewModel.showProtectionSheet = true
                                        },
                                        selectedLevel = selectedLevel,
                                        supportUs = {
                                            uiScope.launch {
                                                viewModel.showSupportSheet = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    )
                    if (viewModel.showSupportSheet) {
                        SupportUsBottomSheet(
                            onDismiss = {
                                viewModel.showSupportSheet = false
                            },
                            sheetState = sheetState
                        )
                    }
                    if(viewModel.showProtectionSheet){
                        EnableProtectionBottomSheet(
                            onDismiss = { viewModel.showProtectionSheet = false },
                            sheetState = sheetState,
                            onConfirm = {
                                // Show confirmation dialog - permission check will happen after user confirms
                                onDialogStateChange(DialogState.EnableProtectionConfirm(selectedLevel))
                                // DO NOT add ProtectionActivated screen here!
                                // It will be added by checkPermissionsAndActivateProtection() after verifying permissions
                                viewModel.showProtectionSheet = false
                            }
                        )
                    }
                },
            )
        }
    )

    // Centralized dialogs rendering
    when (dialogState) {
        is DialogState.ReportProblem -> {
            ReportProblemDialog(
                onClose = { onDialogStateChange(null) },
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
                                    getString(context,R.string.report_send__faild_message),
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                            else -> {}
                        }
                    }
                    onDialogStateChange(null)
                }
            )
        }

        is DialogState.FirstTime -> {
            OkDialog(
                title = stringResource(R.string.test_version_text),
                message = stringResource(R.string.test_version_message).trimIndent(),
                onDismiss = {
                    onDialogStateChange(null)
                    MyApp.isFirstTime = false
                }
            )
        }

        is DialogState.Permission -> {
            PermissionDialog(
                permissionState = dialogState.permission,
                onDismiss = { onDialogStateChange(null) },
                onClick = {
                    grantPermission(dialogState.permission)
                    onDialogStateChange(null)
                }
            )
        }

        is DialogState.BlockApps -> {
            BlockAppDialog(
                onDismiss = { onDialogStateChange(null)},
                appStates = apps,
                onBlockClick = { app ->
                    onDialogStateChange(DialogState.BlockApps(confirmApp = app))
                }
            )
            if (dialogState.confirmApp != null) {
                ConfirmBlockedDialog(
                    app = dialogState.confirmApp,
                    onDismiss = { onDialogStateChange(DialogState.BlockApps()) },
                    onConfirm = {
                        viewModel.toggleAppSelection(it.packageName)
                        onDialogStateChange(DialogState.BlockApps())
                    }
                )
            }
        }

        is DialogState.HowItWorks -> {
            HowItWorksDialog(
                onDismiss = { onDialogStateChange(null) },
                onContactClicked = { context.openUrl(SUPPORT_CONTACT_URL) },
                onSafeSearchClicked = { context.openUrl(SAFE_SEARCH_URL) },
                image = stringResource(R.string.howtoknow_asset).toUri()
            )
        }

        is DialogState.EnableProtectionConfirm -> {
            EnableProtectionDialog(
                onConfirm = {
                    onDialogStateChange(null) // Close the confirmation dialog
                    viewModel.saveLevel(dialogState.level)

                    checkPermissionsAndActivateProtection(
                        context = context,
                        viewModel = viewModel,
                        backStack = backStack,
                        findNextMissingPermission = findNextMissingPermission,
                        onDialogStateChange = onDialogStateChange
                    )
                },
                onDismiss = { onDialogStateChange(null) }
            )
        }
        else -> {}
    }


}

