package com.mafazaa.ainaa.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.mafazaa.ainaa.R

val ElMessiri = FontFamily(
    Font(R.font.elmessiri_bold, weight = FontWeight.Bold),
    Font(R.font.elmessiri_semibold, weight = FontWeight.SemiBold),
    Font(R.font.elmessiri_medium, weight = FontWeight.Medium),
    Font(R.font.elmessiri_regular, weight = FontWeight.Normal),
)

private val defaultTypography = Typography()

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(
        fontFamily = ElMessiri,
        fontSize = 38.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 59.4.sp
    ),
    displayMedium = defaultTypography.displayMedium.copy(
        fontFamily = ElMessiri,
        fontSize = 35.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 54.7.sp
    ),
    displaySmall = defaultTypography.displaySmall.copy(
        fontFamily = ElMessiri,
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 43.8.sp
    ),
    headlineLarge = defaultTypography.headlineLarge.copy(
        fontFamily = ElMessiri,
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 39.1.sp
    ),
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = ElMessiri,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 34.4.sp
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = ElMessiri,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 31.3.sp
    ),
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = ElMessiri,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 28.1.sp
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = ElMessiri,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 25.sp

    ),
    titleSmall = defaultTypography.titleSmall.copy(
        fontFamily = ElMessiri,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 21.9.sp
    ),
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = ElMessiri,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 25.sp
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = ElMessiri,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.9.sp
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = ElMessiri,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 18.8.sp
    ),
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = ElMessiri,
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 21.9.sp
    ),
    labelMedium = defaultTypography.labelMedium.copy(
        fontFamily = ElMessiri,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.8.sp
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = ElMessiri,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 15.6.sp
    )
)