package com.mafazaa.ainaa.viewmodels

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mafazaa.ainaa.BuildConfig
import com.mafazaa.ainaa.utils.MyLog
import com.mafazaa.ainaa.data.local.SharedPrefs
import com.mafazaa.ainaa.data.local.add
import com.mafazaa.ainaa.data.local.remove
import com.mafazaa.ainaa.data.models.NetworkResult
import com.mafazaa.ainaa.domain.models.DnsProtectionLevel
import com.mafazaa.ainaa.domain.FileRepo
import com.mafazaa.ainaa.data.models.ReportModel
import com.mafazaa.ainaa.domain.models.UpdateState
import com.mafazaa.ainaa.domain.repo.RemoteRepo
import com.mafazaa.ainaa.domain.repo.UpdateRepo
import com.mafazaa.ainaa.helpers.ScreenshotOverlayManager
import com.mafazaa.ainaa.domain.models.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class AppViewModel(
    private val remoteRepo: RemoteRepo,
    private val sharedPrefs: SharedPrefs,
    private val fileRepo: FileRepo,
    private val updateRepo: UpdateRepo,
    private val screenshotOverlayManager: ScreenshotOverlayManager
) : ViewModel() {

    private val TAG = "MainViewModel"
    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    var updateState = mutableStateOf<UpdateState>(UpdateState.NoUpdate)
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



    fun submitReport(reportModel: ReportModel, result:  (NetworkResult) -> Unit) {
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