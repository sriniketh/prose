package com.sriniketh.core_design.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.BrushPainter

@Composable
fun gradientPlaceholder() = BrushPainter(
    Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surfaceVariant
        )
    )
)
