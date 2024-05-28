package com.sriniketh.core_design.ui.components

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AppSurface(
    content: @Composable () -> Unit
) {
    Surface(
        tonalElevation = 5.dp,
        shadowElevation = 5.dp
    ) {
        content()
    }
}
