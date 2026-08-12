package com.mirabilis.core.designsystem.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

private val small = RoundedCornerShape(8.dp)
private val medium = RoundedCornerShape(12.dp)
private val large = RoundedCornerShape(16.dp)

fun createMirabilisShapes(): Shapes = Shapes(
    small = small,
    medium = medium,
    large = large,
)
