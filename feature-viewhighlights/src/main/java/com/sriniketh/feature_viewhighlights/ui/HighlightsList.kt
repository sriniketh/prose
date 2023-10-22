package com.sriniketh.feature_viewhighlights.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sriniketh.feature_viewhighlights.HighlightUIState
import com.sriniketh.feature_viewhighlights.R
import com.sriniketh.feature_viewhighlights.ViewHighlightsUIState

@Composable
internal fun HighlightsList(
    modifier: Modifier, innerPadding: PaddingValues, uiState: ViewHighlightsUIState.Success
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        itemsIndexed(uiState.highlightsUIState) { index, highlightUiState ->

            var showDeleteDialog by remember { mutableStateOf(false) }
            if (showDeleteDialog) {
                DeleteHighlightAlertDialog(highlightUiState) {
                    showDeleteDialog = false
                }
            }

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        modifier = modifier.padding(6.dp), text = highlightUiState.text
                    )
                    Text(
                        modifier = modifier.padding(6.dp), text = stringResource(
                            id = R.string.highlight_item_saved_on_template,
                            formatArgs = arrayOf(highlightUiState.savedOn)
                        )
                    )
                }
                IconButton(modifier = modifier
                    .padding(12.dp)
                    .align(Alignment.CenterVertically),
                    onClick = { showDeleteDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.highlight_item_delete_cont_desc)
                    )
                }
            }
            if (index < uiState.highlightsUIState.size) {
                Divider(
                    Modifier.fillMaxWidth(),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun DeleteHighlightAlertDialog(
    highlightUiState: HighlightUIState, hideDeleteDialog: () -> Unit
) {
    AlertDialog(title = { Text(text = stringResource(id = R.string.delete_dialog_title)) },
        text = { Text(text = stringResource(id = R.string.delete_dialog_message)) },
        confirmButton = {
            TextButton(onClick = {
                highlightUiState.onDelete()
                hideDeleteDialog()
            }) {
                Text(text = stringResource(id = R.string.delete_dialog_positive_button_label))
            }
        },
        dismissButton = {
            TextButton(onClick = { hideDeleteDialog() }) {
                Text(text = stringResource(id = R.string.delete_dialog_negative_button_label))
            }
        },
        onDismissRequest = { hideDeleteDialog() })
}

@Preview(showBackground = true)
@Composable
private fun HighlightsListPreview() {
    HighlightsList(
        modifier = Modifier,
        innerPadding = PaddingValues(12.dp),
        uiState = ViewHighlightsUIState.Success(
            highlightsUIState = listOf(HighlightUIState(
                text = "some highlight text 1", savedOn = "12-12-2022"
            ) {}, HighlightUIState(
                text = "some highlight text 2", savedOn = "12-13-2022"
            ) {})
        )
    )
}
