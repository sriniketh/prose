package com.sriniketh.core_design.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sriniketh.core_design.R

@Composable
fun NavigationBack(action: () -> Unit) {
    IconButton(onClick = action) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.nav_back_arrow_cont_desc)
        )
    }
}
