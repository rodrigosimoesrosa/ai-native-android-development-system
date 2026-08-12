package com.mirabilis.core.designsystem.token

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Roboto = FontFamily.Default

private val displayLarge = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 57.sp,
    lineHeight = 64.sp,
    letterSpacing = (-0.25).sp,
)

private val displayMedium = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 45.sp,
    lineHeight = 52.sp,
    letterSpacing = 0.sp,
)

private val displaySmall = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 36.sp,
    lineHeight = 44.sp,
    letterSpacing = 0.sp,
)

private val headlineLarge = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 32.sp,
    lineHeight = 40.sp,
    letterSpacing = 0.sp,
)

private val headlineMedium = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 28.sp,
    lineHeight = 36.sp,
    letterSpacing = 0.sp,
)

private val headlineSmall = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 24.sp,
    lineHeight = 32.sp,
    letterSpacing = 0.25.sp,
)

private val titleLarge = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 22.sp,
    lineHeight = 28.sp,
    letterSpacing = 0.sp,
)

private val titleMedium = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
)

private val titleSmall = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)

private val bodyLarge = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.5.sp,
)

private val bodyMedium = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.25.sp,
)

private val bodySmall = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W400,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
)

private val labelLarge = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)

private val labelMedium = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 12.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.4.sp,
)

private val labelSmall = TextStyle(
    fontFamily = Roboto,
    fontWeight = FontWeight.W500,
    fontSize = 11.sp,
    lineHeight = 16.sp,
    letterSpacing = 0.5.sp,
)

fun createMirabilisTypography(): Typography = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall,
)
