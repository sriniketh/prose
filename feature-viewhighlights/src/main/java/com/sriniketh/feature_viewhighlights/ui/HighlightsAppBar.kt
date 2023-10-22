package com.sriniketh.feature_viewhighlights.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.sriniketh.feature_viewhighlights.R

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun HighlightsAppBar(goBack: () -> Unit) {
    TopAppBar(title = {
        Text(stringResource(id = R.string.highlights_pagetitle))
    }, navigationIcon = {
        IconButton(onClick = goBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(id = R.string.nav_icon_back_cont_desc)
            )
        }
    })
}

@Preview(showBackground = true)
@Composable
private fun HighlightsAppBarPreview() {
    HighlightsAppBar {}
}
