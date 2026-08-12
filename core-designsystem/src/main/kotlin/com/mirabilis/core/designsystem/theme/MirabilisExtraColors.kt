package com.mirabilis.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class MirabilisExtraColors(
    val warning: Color,
    val onWarning: Color,
    val success: Color,
    val onSuccess: Color,
)

val LocalMirabilisExtraColors = compositionLocalOf {
    MirabilisExtraColors(
        warning = Color.Unspecified,
        onWarning = Color.Unspecified,
        success = Color.Unspecified,
        onSuccess = Color.Unspecified,
    )
}
