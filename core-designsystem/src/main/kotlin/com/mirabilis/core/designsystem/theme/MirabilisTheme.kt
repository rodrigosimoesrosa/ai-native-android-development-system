package com.mirabilis.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import com.mirabilis.core.designsystem.token.createMirabilisShapes
import com.mirabilis.core.designsystem.token.createMirabilisTypography
import com.mirabilis.core.designsystem.token.darkColorScheme
import com.mirabilis.core.designsystem.token.darkExtraColors
import com.mirabilis.core.designsystem.token.lightColorScheme
import com.mirabilis.core.designsystem.token.lightExtraColors

@Composable
fun MirabilisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = remember(darkTheme) {
        if (darkTheme) darkColorScheme() else lightColorScheme()
    }
    val typography = remember { createMirabilisTypography() }
    val shapes = remember { createMirabilisShapes() }
    val extraColors = remember(darkTheme) {
        if (darkTheme) darkExtraColors() else lightExtraColors()
    }

    CompositionLocalProvider(
        LocalMirabilisExtraColors provides extraColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            shapes = shapes,
            content = content,
        )
    }
}

object MirabilisTheme {
    val extraColors: MirabilisExtraColors
        @Composable
        @ReadOnlyComposable
        get() = LocalMirabilisExtraColors.current
}
