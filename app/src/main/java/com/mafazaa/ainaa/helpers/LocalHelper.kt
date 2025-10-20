package com.mafazaa.ainaa.helpers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * A singleton object responsible for handling application-wide locale changes.
 * This helper provides functionality to set the application's language, specifically to Arabic.
 * It ensures compatibility across different Android API levels by using modern and legacy methods
 * for locale updates.
 */
object LocaleHelper {
    fun forceArabicLocale(context: Context): Context {
        val languageTag = "ar"

        val localeList = LocaleListCompat.forLanguageTags(languageTag)
        AppCompatDelegate.setApplicationLocales(localeList)

        val locale = Locale(languageTag)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                @Suppress("DEPRECATION")
                config.setLayoutDirection(locale)
            }
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
}
