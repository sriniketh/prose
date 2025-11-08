package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sriniketh.core_design.ui.components.NavigationBack
import com.sriniketh.core_design.ui.components.ProseTopAppBar
import com.sriniketh.core_design.ui.theme.AppTheme
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun EditAndSaveHighlight(
    uiState: EditAndSaveHighlightUiState,
    updateHighlightText: (String) -> Unit,
    saveHighlight: (String) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ProseTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = uiState.screenTitle),
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { NavigationBack { goBack() } },
                actions = {
                    TooltipBox(
                        positionProvider =
                            TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = { PlainTooltip { Text(stringResource(id = R.string.save_button_cont_desc)) } },
                        state = rememberTooltipState(),
                    ) {
                        FilledIconButton(
                            modifier = modifier.testTag("SaveHighlightButton"),
                            onClick = {
                                saveHighlight(uiState.highlightText)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            shape = IconButtonDefaults.smallPressedShape,
                            enabled = !uiState.isLoading
                        ) {
                            Row {
                                Icon(
                                    painter = painterResource(com.sriniketh.core_design.R.drawable.ic_done),
                                    contentDescription = stringResource(id = R.string.save_button_cont_desc)
                                )
                            }
                        }
                    }
                }
            )
        },
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = modifier
                    .fillMaxWidth()
                    .testTag("AddHighlightLoadingIndicator")
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

        Column(
            modifier = modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            BasicTextField(
                modifier = modifier
                    .weight(0.8f)
                    .fillMaxWidth()
                    .padding(18.dp)
                    .testTag("AddHighlightTextField")
                    .focusRequester(focusRequester),
                value = uiState.highlightText,
                onValueChange = { updateHighlightText(it) },
                enabled = !uiState.isLoading,
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
            )

            LaunchedEffect(uiState.isLoading) {
                if (!uiState.isLoading) {
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
internal fun EditAndSaveHighlightPreview() {
    AppTheme {
        EditAndSaveHighlight(
            uiState = EditAndSaveHighlightUiState(
                highlightText = "This is a highlight text",
            ),
            updateHighlightText = {},
            saveHighlight = {},
            goBack = {}
        )
    }
}
