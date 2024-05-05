package com.sriniketh.feature_viewhighlights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sriniketh.core_design.ui.components.NavigationBack
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewHighlightsScreen(
    uiState: ViewHighlightsUIState,
    modifier: Modifier = Modifier,
    addHighlight: () -> Unit,
    goBack: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.highlights_pagetitle),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { NavigationBack(goBack) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = addHighlight,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.add_fab_cont_desc)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(contentPadding)
            )
        }

        uiState.snackBarText?.let { resId ->
            val snackbarMessage = stringResource(id = resId)
            LaunchedEffect(key1 = null) {
                launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }
        }

        if (uiState.highlights.isEmpty()) {
            Box(
                modifier = modifier.fillMaxSize()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(id = R.string.no_highlights_text),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                items(uiState.highlights) { highlightUiState ->
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
                        Column(modifier = modifier.weight(1f)) {
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = highlightUiState.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = stringResource(
                                    id = R.string.highlight_item_saved_on_template,
                                    highlightUiState.savedOn
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(
                            modifier = modifier
                                .padding(6.dp)
                                .align(Alignment.CenterVertically),
                            onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.highlight_item_delete_cont_desc)
                            )
                        }
                    }
                    HorizontalDivider(modifier = modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
private fun DeleteHighlightAlertDialog(
    highlightUiState: HighlightUIState,
    hideDeleteDialog: () -> Unit
) {
    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.delete_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.delete_dialog_message),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    highlightUiState.onDelete()
                    hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(id = R.string.delete_dialog_positive_button_label),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { hideDeleteDialog() }) {
                Text(
                    text = stringResource(id = R.string.delete_dialog_negative_button_label),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        },
        onDismissRequest = { hideDeleteDialog() })
}
