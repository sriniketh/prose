package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
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
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is EditAndSaveHighlightEffect.ShowMessage -> scope.launch {
                        snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
                    }

                    EditAndSaveHighlightEffect.HighlightSaved -> goBack()
                }
            }
        }
    }

    EditAndSaveHighlight(
        modifier = modifier,
        uiState = editHighlightUiState,
        snackbarHostState = snackbarHostState,
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
    LaunchedEffect(key1 = highlightId) {
        viewModel.loadHighlightText(highlightId)
    }

    val editHighlightUiState: EditAndSaveHighlightUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is EditAndSaveHighlightEffect.ShowMessage -> scope.launch {
                        snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
                    }

                    EditAndSaveHighlightEffect.HighlightSaved -> goBack()
                }
            }
        }
    }

    EditAndSaveHighlight(
        modifier = modifier,
        uiState = editHighlightUiState,
        snackbarHostState = snackbarHostState,
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
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
                navigationIcon = { NavigationBack { goBack() } }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                modifier = Modifier
                    .testTag("SaveHighlightButton")
                    .alpha(if (uiState.isLoading) 0.38f else 1f),
                onClick = {
                    if (!uiState.isLoading) {
                        saveHighlight(uiState.highlightText)
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        painter = painterResource(com.sriniketh.core_design.R.drawable.ic_done),
                        contentDescription = stringResource(id = R.string.save_button_cont_desc)
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.done_button_label),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        },
    ) { contentPadding ->
        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("AddHighlightLoadingIndicator")
            )
        }

        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            BasicTextField(
                modifier = Modifier
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
