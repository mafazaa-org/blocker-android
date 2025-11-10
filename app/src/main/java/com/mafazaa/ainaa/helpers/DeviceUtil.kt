package com.mafazaa.ainaa.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import com.mafazaa.ainaa.utils.MyLog
import org.koin.java.KoinJavaComponent

/**
 * A singleton object to hold device-specific information that is expensive to query.
 * We use 'lazy' to ensure the settings package name is queried only once.
 */
object DeviceUtils {

    // This property will store the name of the settings package.
    // It's computed only once when first accessed and then cached.
    val settingsPackageName: String by lazy {
        val context: Context by KoinJavaComponent.inject(Context::class.java)
        querySettingPkgName(context)
    }

    /**
     * Programmatically queries the system for the default settings application package name.
     * This is far more reliable than a hardcoded list.
     *
     * @param context The application context to access the PackageManager.
     * @return The package name of the default settings app, or an empty string if not found.
     */
    private fun querySettingPkgName(context: Context): String {
        val intent = Intent(Settings.ACTION_SETTINGS)
        val resolveInfo = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        MyLog.d("DeviceUtils", "Queried settings package name: ${resolveInfo?.activityInfo?.packageName}")
        return resolveInfo?.activityInfo?.packageName ?: ""
    }
}
