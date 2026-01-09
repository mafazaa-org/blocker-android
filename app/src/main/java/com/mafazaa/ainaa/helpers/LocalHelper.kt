package com.mafazaa.ainaa.helpers

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

/**
 * A singleton object responsible for handling application-wide locale changes.
 * This helper provides functionality to adapt the application's language and layout direction
 * based on the system locale, supporting both RTL (Arabic) and LTR (English) languages.
 * It ensures compatibility across different Android API levels by using modern and legacy methods
 * for locale updates.
 */
object LocaleHelper {
    /**
     * Configures the app to use the system's locale and layout direction.
     * This allows the app to support both Arabic (RTL) and English (LTR) automatically.
     *
     * @param context The application context
     * @return Context with the system locale configuration applied
     */
    fun applySystemLocale(context: Context): Context {
        // Get the system's default locale
        val systemLocale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }

        // Use system locale
        Locale.setDefault(systemLocale)

        val config = Configuration(context.resources.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(systemLocale)
            config.setLayoutDirection(systemLocale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = systemLocale
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                @Suppress("DEPRECATION")
                config.setLayoutDirection(systemLocale)
            }
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }

    /**
     * Legacy method kept for backward compatibility.
     * Now delegates to applySystemLocale.
     */
    @Deprecated("Use applySystemLocale instead", ReplaceWith("applySystemLocale(context)"))
    fun forceArabicLocale(context: Context): Context {
        return applySystemLocale(context)
    }

    /**
     * Checks if the current locale is RTL (Right-to-Left).
     *
     * @param context The application context
     * @return true if the current locale uses RTL layout, false otherwise
     */
    fun isRtl(context: Context): Boolean {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            android.text.TextUtils.getLayoutDirectionFromLocale(locale) == android.view.View.LAYOUT_DIRECTION_RTL
        } else {
            // Fallback for older versions - check common RTL language codes
            locale.language in listOf("ar", "fa", "he", "iw", "ji", "ur", "yi")
        }
    }
}
