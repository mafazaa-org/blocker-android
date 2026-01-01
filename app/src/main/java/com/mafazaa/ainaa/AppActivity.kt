package com.mafazaa.ainaa

import android.Manifest.permission.POST_NOTIFICATIONS
import android.R.attr.data
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.ViewCompat.setLayoutDirection
import androidx.lifecycle.lifecycleScope
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.domain.models.AppInfo
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.helpers.LocaleHelper
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.receiver.AppDeviceAdminReceiver
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyAccessibilityService.Companion.startAccessibilityService
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.ui.common.OkDialog
import com.mafazaa.ainaa.ui.dialog.BlockAppDialog
import com.mafazaa.ainaa.ui.dialog.ConfirmBlockedDialog
import com.mafazaa.ainaa.ui.dialog.EnableProtectionDialog
import com.mafazaa.ainaa.ui.dialog.HowItWorksDialog
import com.mafazaa.ainaa.ui.dialog.ManageKeywordsDialog
import com.mafazaa.ainaa.ui.dialog.PermissionDialog
import com.mafazaa.ainaa.ui.dialog.ReportProblemDialog
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.utils.Constants.SAFE_SEARCH_URL
import com.mafazaa.ainaa.utils.Constants.SUPPORT_CONTACT_URL
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.getAllApps
import com.mafazaa.ainaa.utils.isServiceRunning
import com.mafazaa.ainaa.utils.openUrl
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
    data object BlockWords : DialogState
    data class EnableProtectionConfirm(val level: DnsProtectionLevel) :
        DialogState
}

class AppActivity : ComponentActivity() {
    var dialogState by mutableStateOf<DialogState?>(if (MyApp.isFirstTime) DialogState.FirstTime else null)


    private val adminReceiver by lazy {
        ComponentName(
            this,
            AppDeviceAdminReceiver::class.java
        )
    }
    private val requestAdmin = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {}
    val viewModel: AppViewModel by lazy {
        getViewModel<AppViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setLayoutDirection(window.decorView, ViewCompat.LAYOUT_DIRECTION_RTL)
        enableEdgeToEdge()
        val splashscreen = installSplashScreen()
        var keepSplashScreen = true
        super.onCreate(savedInstanceState)
        splashscreen.setKeepOnScreenCondition { keepSplashScreen }
        lifecycleScope.launch {
            delay(3000)
            keepSplashScreen = false
        }

        val sharedPrefs: SharedPrefs by inject(SharedPrefs::class.java)
        viewModel.loadInstalledApps(getAllApps())
        viewModel.loadBlockedWords()
        MyLog.i(TAG, "Opening app")
        viewModel.refreshPermissionState()

        requestAdminPermission(adminReceiver, requestAdmin)

        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                AinaaTheme {
                    MainRoot(
                        viewModel = viewModel,
                        sharedPrefs = sharedPrefs,
                        dialogState = dialogState,
                        onDialogStateChange = { dialogState = it },
                        grantPermission = { permissionState -> grantPermission(permissionState) },
                        findNextMissingPermission = { viewModel.permissionState?: PermissionState.Granted },
                        permissionDialogChecker = {}
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshPermissionState()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
       viewModel.refreshPermissionState()
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