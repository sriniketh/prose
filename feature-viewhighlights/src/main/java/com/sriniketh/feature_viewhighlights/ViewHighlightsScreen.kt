package com.sriniketh.feature_viewhighlights

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sriniketh.core_design.ui.components.NavigationBack
import kotlinx.coroutines.launch

@Composable
fun ViewHighlightsScreen(
    modifier: Modifier = Modifier,
    viewModel: ViewHighlightsViewModel = hiltViewModel(),
    bookId: String,
    goBack: () -> Unit,
    goToAddHighlightScreen: () -> Unit,
    goToEditHighlightScreen: (String) -> Unit
) {
    LaunchedEffect(key1 = bookId) {
        viewModel.getHighlights(bookId)
    }
    val uiState: ViewHighlightsUIState by viewModel.highlightsUIStateFlow.collectAsStateWithLifecycle()
    ViewHighlights(
        modifier = modifier,
        uiState = uiState,
        onEvent = { event ->
            when (event) {
                is ViewHighlightsEvent.OnBackPressed -> {
                    goBack()
                }

                is ViewHighlightsEvent.OnCameraPermissionGranted -> {
                    goToAddHighlightScreen()
                }

                is ViewHighlightsEvent.OnEditHighlight -> {
                    goToEditHighlightScreen(event.highlightId)
                }

                else -> viewModel.processEvent(event)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ViewHighlights(
    uiState: ViewHighlightsUIState,
    modifier: Modifier = Modifier,
    onEvent: (ViewHighlightsEvent) -> Unit
) {
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                onEvent(ViewHighlightsEvent.OnCameraPermissionGranted)
            } else {
                onEvent(ViewHighlightsEvent.OnCameraPermissionDenied)
            }
        })
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val shouldFabBeVisible by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex < 2
        }
    }
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
                navigationIcon = { NavigationBack { onEvent(ViewHighlightsEvent.OnBackPressed) } }
            )
        },
        floatingActionButton = {
            if (shouldFabBeVisible) {
                FloatingActionButton(
                    onClick = { cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = stringResource(id = R.string.add_fab_cont_desc)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->
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
                    .padding(contentPadding),
                state = lazyListState
            ) {
                items(uiState.highlights) { highlightUiState ->
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    if (showDeleteDialog) {
                        DeleteHighlightAlertDialog(highlightUiState) {
                            showDeleteDialog = false
                        }
                    }

                    var expandDropDownMenu by remember { mutableStateOf(false) }
                    val shortenHighlightText by remember { derivedStateOf { highlightUiState.text.length > 250 } }
                    var highlightText by remember {
                        if (shortenHighlightText) {
                            mutableStateOf(highlightUiState.text.take(250) + " ...")
                        } else {
                            mutableStateOf(highlightUiState.text)
                        }
                    }

                    Row(
                        modifier = modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .animateContentSize()
                            .clickable {
                                if (shortenHighlightText) {
                                    highlightText = highlightUiState.text
                                }
                            },
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = modifier.weight(1f)) {
                            SelectionContainer {
                                Text(
                                    modifier = modifier.padding(6.dp),
                                    text = highlightText,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Text(
                                modifier = modifier.padding(6.dp),
                                text = stringResource(
                                    id = R.string.highlight_item_saved_on_template,
                                    highlightUiState.savedOn
                                ),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Box(
                            modifier = Modifier
                                .wrapContentSize(Alignment.TopStart)
                                .align(Alignment.Top)
                        ) {
                            IconButton(onClick = { expandDropDownMenu = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Localized description"
                                )
                            }
                            DropdownMenu(
                                modifier = modifier.clickable { expandDropDownMenu = true },
                                expanded = expandDropDownMenu,
                                onDismissRequest = { expandDropDownMenu = false }) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.highlight_menu_item_copy),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(highlightUiState.text))
                                        expandDropDownMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.highlight_menu_item_edit),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    onClick = {
                                        onEvent(ViewHighlightsEvent.OnEditHighlight(highlightUiState.id))
                                        expandDropDownMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = stringResource(id = R.string.highlight_menu_item_delete),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    },
                                    onClick = {
                                        showDeleteDialog = true
                                        expandDropDownMenu = false
                                    }
                                )
                            }
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
    val haptic = LocalHapticFeedback.current
    AlertDialog(
        title = {
            Text(
                text = stringResource(id = R.string.delete_dialog_title),
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.delete_dialog_message),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            ElevatedButton(
                onClick = {
                    highlightUiState.onDelete()
                    hideDeleteDialog()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                colors = ButtonDefaults.buttonColors()
            ) {
                Text(
                    text = stringResource(id = R.string.delete_dialog_positive_button_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { hideDeleteDialog() }) {
                Text(
                    text = stringResource(id = R.string.delete_dialog_negative_button_label),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        onDismissRequest = { hideDeleteDialog() })
}
