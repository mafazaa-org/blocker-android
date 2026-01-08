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
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.ui.common.EnableProtectionBottomSheet
import com.mafazaa.ainaa.ui.common.MainDrawer
import com.mafazaa.ainaa.ui.common.OkDialog
import com.mafazaa.ainaa.ui.common.SupportUsBottomSheet
import com.mafazaa.ainaa.ui.common.TopBar
import com.mafazaa.ainaa.ui.dialog.BlockAppDialog
import com.mafazaa.ainaa.ui.dialog.ConfirmBlockedDialog
import com.mafazaa.ainaa.ui.dialog.HowItWorksDialog
import com.mafazaa.ainaa.ui.dialog.PermissionDialog
import com.mafazaa.ainaa.ui.dialog.ReportProblemDialog
import com.mafazaa.ainaa.ui.protection.EnableProtectionScreen
import com.mafazaa.ainaa.ui.protection.ProtectionActivatedScreen
import com.mafazaa.ainaa.utils.Constants.SAFE_SEARCH_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_CONTACT_URL
import com.mafazaa.ainaa.utils.installApk
import com.mafazaa.ainaa.utils.openUrl
import com.mafazaa.ainaa.viewmodels.AppViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoot(
    context: Context = LocalContext.current,
    viewModel: AppViewModel,
    dialogState: DialogState?,
    onDialogStateChange: (DialogState?) -> Unit,
    grantPermission: (PermissionState) -> Unit,
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

                                            // Check only non-critical permissions (Notification and Overlay)
                                            // VPN, Accessibility, and Admin will be requested in the bottom sheet
                                            val missingPermission = viewModel.checkNonCriticalPermissions()
                                            if (missingPermission != null) {
                                                // Show permission dialog for the missing non-critical permission
                                                onDialogStateChange(DialogState.Permission(missingPermission))
                                                return@EnableProtectionScreen
                                            }

                                            // All non-critical permissions granted, show the bottom sheet
                                            onSelectedLevelChange(level)
                                            viewModel.showProtectionSheet = true
                                        },
                                        selectedLevel = selectedLevel,
                                        onSelectedLevelChange = onSelectedLevelChange,
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
                            sheetState = sheetState,
                            amount = viewModel.supportAmount.toString(),
                            onAmountChange = { viewModel.supportAmount = it.toInt() },
                            paymentMethod = viewModel.paymentMethod
                        )
                    }
                    if(viewModel.showProtectionSheet){
                        EnableProtectionBottomSheet(
                            title = if (selectedLevel == DnsProtectionLevel.HIGH)
                                stringResource(R.string.enable_protection_lvl_high_text)
                            else stringResource(R.string.enable_protection_lvl_low_text),
                            onDismiss = { viewModel.showProtectionSheet = false },
                            sheetState = sheetState,
                            viewModel = viewModel,
                            onConfirm = {
                                // Save the selected protection level
                                viewModel.saveLevel(selectedLevel)

                                // Close the bottom sheet first
                                viewModel.showProtectionSheet = false

                                // Request critical permissions (VPN, Accessibility, Admin if needed)
                                // This will show permission dialogs one by one
                                viewModel.requestCriticalPermissions(
                                    onDialogStateChange = onDialogStateChange
                                )

                                // If all permissions are already granted, directly check services and navigate
                                // (onResume won't be called if we don't leave the app)
                                viewModel.checkServicesAndNavigate(backStack)
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

        else -> {}
    }


}

