package com.mafazaa.ainaa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary, // Red
    onPrimary = Color.White, // White text on Red buttons

    secondary = lightGray, // Neutral gray for secondary elements
    onSecondary = Color.Black,

    tertiary = gray, // Another neutral gray
    onTertiary = Color.White,

    background = DarkBackground, // Pure Black
    onBackground = DarkOnBackground, // White text on Black background

    surface = DarkSurface, // Very dark gray for surfaces like cards
    onSurface = DarkOnSurface, // White text on surfaces

    surfaceVariant = gray,
    onSurfaceVariant = Color.White,

    error = DarkError, // A specific, softer red for errors
    onError = Color.Black, // Black text on the error color for good contrast

    outline = gray
)

private val LightColorScheme = lightColorScheme(
    primary = red,
    secondary = PurpleGrey40,
    tertiary = Pink40,

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun AinaaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
   //         if (darkTheme) dynamicDarkColorScheme(context) else
                dynamicLightColorScheme(context)
        }

        //darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                content()
            }
        }
    )
}
