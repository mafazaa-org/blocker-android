package com.mafazaa.ainaa.utils

import android.Manifest.permission.POST_NOTIFICATIONS
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.AppOpsManager
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.VpnService.prepare
import android.os.Build
import android.os.Process
import android.provider.Settings
import android.provider.Settings.canDrawOverlays
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.mafazaa.ainaa.R
import com.mafazaa.ainaa.service.MyAccessibilityService
import com.mafazaa.ainaa.service.MyVpnService
import com.mafazaa.ainaa.domain.models.AppInfo
import java.io.File

/*
 * Context extension functions for various utilities.
 */
fun Context.installApk(apkFile: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    val apkUri =
        FileProvider.getUriForFile(
            this,
            "${this.packageName}.provider",
            apkFile
        )
    intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
    try {
        this.startActivity(intent)
    } catch (e: Exception) {
        MyLog.e("InstallApk", "Error starting install", e)
    }
}

fun Context.startVpnService() {

    val intent = Intent(this, MyVpnService::class.java).apply {
        action = MyVpnService.ACTION_START
    }
    prepare(this)
    ContextCompat.startForegroundService(this, intent)
    MyVpnService.isRunning = true
}

fun Context.openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
    intent.addCategory(Intent.CATEGORY_BROWSABLE)
    startActivity(intent)
}

fun Context.hasOverlayPermission(): Boolean = canDrawOverlays(this)
fun Context.hasUsageStatsPermission(): Boolean {
    val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
    } else {
        appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(), packageName
        )
    }
    return mode == AppOpsManager.MODE_ALLOWED
}

fun Context.hasNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
}

fun Context.hasVpnPermission(): Boolean = prepare(this) == null

fun Context.isKeyguardSecure(): Boolean {
    val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    return keyguardManager.isKeyguardSecure
}

fun ComponentActivity.requestDrawOverlaysPermission() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    startActivity(intent)
}

fun ComponentActivity.requestVpnPermission() {
    val intent = prepare(this)
    if (intent != null) {
        startActivityForResult(intent, 0)
    }
}

fun ComponentActivity.requestUsageStatsPermission() {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    startActivity(intent)
}

fun Context.hasAccessibilityPermission(): Boolean {
    val am =
        getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    val enabledServices =
        am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
    for (service in enabledServices) {
        if (service.resolveInfo.serviceInfo.packageName == packageName &&
            service.resolveInfo.serviceInfo.name == MyAccessibilityService::class.java.name
        ) {
            return true
        }
    }
    return false
}

fun Context.requestAccessibilityPermission() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}

fun Context.shareFile(logFile: File) {
    val uri = FileProvider.getUriForFile(
        this,
        "${packageName}.provider",
        logFile
    )

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    startActivity(Intent.createChooser(shareIntent, "Share log file").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}
fun Context.hasAdminPermission(
    adminReceiver: android.content.ComponentName
): Boolean {
    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
    return dpm.isAdminActive(adminReceiver)
}

fun Context.requestAdminPermission(
    
    adminReceiver: android.content.ComponentName,
    requestAdmin: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
    val isAdminActive = dpm.isAdminActive(adminReceiver)
    if (!isAdminActive) {
        val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver)
        intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            getString(R.string.admin_permission_message))
        requestAdmin.launch(intent)
    }
}

fun Context.getAllApps(): List<AppInfo> {
    val apps = mutableListOf<AppInfo>()
    val packageManager = this.packageManager
    val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

    for (applicationInfo in packages) {
        if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0 && applicationInfo.packageName!= this.packageName) {
            val name = packageManager.getApplicationLabel(applicationInfo).toString()
            val icon = packageManager.getApplicationIcon(applicationInfo)
            apps.add(AppInfo(name, icon, applicationInfo.packageName))
        }
    }
    return apps
}