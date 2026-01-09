package com.mafazaa.ainaa

import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import com.mafazaa.ainaa.domain.models.PermissionState
import com.mafazaa.ainaa.helpers.LocaleHelper
import com.mafazaa.ainaa.receiver.AppDeviceAdminReceiver
import com.mafazaa.ainaa.ui.theme.AinaaTheme
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.getAllApps
import com.mafazaa.ainaa.utils.requestAccessibilityPermission
import com.mafazaa.ainaa.utils.requestAdminPermission
import com.mafazaa.ainaa.utils.requestDrawOverlaysPermission
import com.mafazaa.ainaa.utils.requestVpnPermission
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
        // Apply system locale and layout direction (supports both RTL and LTR)
        val isRtl = LocaleHelper.isRtl(this)
        val layoutDirection = if (isRtl) {
            ViewCompat.LAYOUT_DIRECTION_RTL
        } else {
            ViewCompat.LAYOUT_DIRECTION_LTR
        }
        setLayoutDirection(window.decorView, layoutDirection)

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


        setContent {
            // Use adaptive layout direction based on system locale
            val composeLayoutDirection = if (isRtl) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides composeLayoutDirection) {
                AinaaTheme {
                    MainRoot(
                        viewModel = viewModel,
                        dialogState = dialogState,
                        onDialogStateChange = { dialogState = it },
                        grantPermission = { permissionState -> grantPermission(permissionState) },
                        permissionDialogChecker = {},
                        selectedLevel = viewModel.selectedLevel,
                        onSelectedLevelChange = { viewModel.selectedLevel = it }
                    )
                }
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // Refresh permission state to detect any changes
        viewModel.refreshPermissionState()

        // Check if services are running and navigate to ProtectionActivated if ready
        // This handles the case when user returns after granting permissions
        viewModel.checkServicesAndNavigate(viewModel.backStack)
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
            PermissionState.Administrative -> {
                // Only request admin permission if uninstall protection is enabled
                if (viewModel.uninstallAppCheck) {
                    requestAdminPermission(adminReceiver, requestAdmin)
                } else {
                    MyLog.w(TAG, "Admin permission requested but uninstallAppCheck is disabled")
                }
            }
            PermissionState.Granted -> {}
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun attachBaseContext(newBase: Context) {
        // Apply system locale (supports both Arabic and English)
        val context = LocaleHelper.applySystemLocale(newBase)
        super.attachBaseContext(context)
    }
}