package com.mafazaa.ainaa.viewmodels

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazaa.ainaa.BuildConfig
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
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import com.mafazaa.ainaa.navigation.Screen
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.utils.hasAccessibilityPermission
import com.mafazaa.ainaa.utils.hasNotificationPermission
import com.mafazaa.ainaa.utils.hasOverlayPermission
import com.mafazaa.ainaa.utils.hasUsageStatsPermission
import com.mafazaa.ainaa.utils.hasVpnPermission
import com.mafazaa.ainaa.utils.isServiceRunning
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
    var permissionState by mutableStateOf<PermissionState?>(null)
    private var notificationPermission by mutableStateOf(
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    )
    var showSupportSheet by  mutableStateOf(false)
    var showProtectionSheet by   mutableStateOf(false)

    val backStack by lazy {
        mutableStateListOf(
            if (!isServiceRunning(context, MyAccessibilityService::class.java)) Screen.EnableProtection
            else Screen.ProtectionActivated
        )
    }
    fun setSupportSheet(value : Boolean) {
        showSupportSheet =  value
    }
    fun setProtectionSheet(value : Boolean) {
        showProtectionSheet = value
    }

    fun refreshPermissionState() {
        if (!notificationPermission) {
            notificationPermission = context.hasNotificationPermission()
        }
        if (vpnPermission) {
            vpnPermission = context.hasVpnPermission()
        }
        if (!overlayPermission) {
            overlayPermission = context.hasOverlayPermission()
        }
        if (!usageStatsPermission) {
            usageStatsPermission = context.hasUsageStatsPermission()
        }
        if (!accessibilityPermission) {
            accessibilityPermission = context.hasAccessibilityPermission()
        }
        permissionState = when {
            !notificationPermission -> PermissionState.Notification
            !vpnPermission -> PermissionState.Vpn
            !overlayPermission -> PermissionState.Overlay
            !accessibilityPermission -> PermissionState.Accessibility
            else -> null
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




}