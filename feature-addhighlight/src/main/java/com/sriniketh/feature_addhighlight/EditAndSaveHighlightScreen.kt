package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sriniketh.core_design.ui.components.NavigationBack
import kotlinx.coroutines.launch

@Composable
fun EditAndSaveHighlightScreen(
    modifier: Modifier = Modifier,
    viewModel: EditAndSaveHighlightViewModel = hiltViewModel(),
    uri: Uri,
    bookId: String,
    goBack: () -> Unit
) {
    LaunchedEffect(key1 = bookId) {
        viewModel.processImageForHighlightText(uri)
    }
    val editHighlightUiState: EditAndSaveHighlightUiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(editHighlightUiState.highlightSaved) {
        if (editHighlightUiState.highlightSaved) {
            goBack()
        }
    }
    EditAndSaveHighlight(
        modifier = modifier,
        uiState = editHighlightUiState,
        updateHighlightText = { highlightText ->
            viewModel.updateHighlightText(highlightText)
        },
        saveHighlight = { highlight ->
            viewModel.saveHighlight(bookId = bookId, highlightText = highlight)
        },
        goBack = {
            goBack()
        }
    )
}

@Composable
fun EditAndSaveHighlightScreen(
    modifier: Modifier = Modifier,
    viewModel: EditAndSaveHighlightViewModel = hiltViewModel(),
    highlightId: String,
    bookId: String,
    goBack: () -> Unit
) {
    LaunchedEffect(key1 = bookId) {
        viewModel.loadHighlightText(highlightId)
    }

    val editHighlightUiState: EditAndSaveHighlightUiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(editHighlightUiState.highlightSaved) {
        if (editHighlightUiState.highlightSaved) {
            goBack()
        }
    }
    EditAndSaveHighlight(
        modifier = modifier,
        uiState = editHighlightUiState,
        updateHighlightText = { highlightText ->
            viewModel.updateHighlightText(highlightText)
        },
        saveHighlight = { highlight ->
            viewModel.updateHighlight(
                bookId = bookId,
                highlightText = highlight,
                highlightId = highlightId
            )
        },
        goBack = {
            goBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditAndSaveHighlight(
    uiState: EditAndSaveHighlightUiState,
    updateHighlightText: (String) -> Unit,
    saveHighlight: (String) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.save_highlight_title_text),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { NavigationBack { goBack() } }
            )
        },
    ) { contentPadding ->
        val haptic = LocalHapticFeedback.current

        if (uiState.isLoading) {
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        }

        uiState.snackBarText?.let { resId ->
            val snackbarMessage = stringResource(id = resId)
            LaunchedEffect(key1 = null) {
                launch {
                    snackbarHostState.showSnackbar(snackbarMessage)
                }
            }
        }

        Column(
            modifier = modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                modifier = modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .padding(12.dp),
                value = uiState.highlightText,
                onValueChange = { updateHighlightText(it) },
                enabled = !uiState.isLoading,
                textStyle = MaterialTheme.typography.bodyLarge
            )
            Row(
                modifier = modifier
                    .weight(0.2f)
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    modifier = modifier.padding(6.dp),
                    enabled = !uiState.isLoading,
                    onClick = { goBack() }
                ) {
                    Text(
                        text = stringResource(id = R.string.cancel_button_text),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                ElevatedButton(
                    modifier = modifier.padding(6.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(),
                    onClick = {
                        saveHighlight(uiState.highlightText)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                ) {
                    Text(
                        text = stringResource(id = R.string.save_button_text),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
