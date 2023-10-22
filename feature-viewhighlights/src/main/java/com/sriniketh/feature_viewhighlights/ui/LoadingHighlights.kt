package com.sriniketh.feature_viewhighlights.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun LoadingHighlights(
    modifier: Modifier, innerPadding: PaddingValues
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        CircularProgressIndicator(
            modifier = modifier
                .width(40.dp)
                .align(Alignment.Center),
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoadingHighlightsPreview() {
    LoadingHighlights(modifier = Modifier, innerPadding = PaddingValues(12.dp))
}
