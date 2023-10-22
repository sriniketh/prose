package com.sriniketh.feature_viewhighlights.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sriniketh.feature_viewhighlights.R

@Composable
internal fun NoHighlights(
    modifier: Modifier, innerPadding: PaddingValues
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            modifier = modifier.align(Alignment.Center),
            text = stringResource(id = R.string.no_highlights_text)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoHighlightsPreview() {
    NoHighlights(modifier = Modifier, innerPadding = PaddingValues(12.dp))
}
