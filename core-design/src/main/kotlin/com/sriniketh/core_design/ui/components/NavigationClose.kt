package com.sriniketh.core_design.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sriniketh.core_design.R

@Composable
fun NavigationClose(action: () -> Unit) {
    IconButton(onClick = action) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(id = R.string.nav_close_cont_desc)
        )
    }
}
