package com.sriniketh.core_design.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.BrushPainter

@Composable
fun gradientPlaceholder(): BrushPainter {
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    return remember(surface, surfaceVariant) {
        BrushPainter(Brush.linearGradient(listOf(surface, surfaceVariant)))
    }
}
