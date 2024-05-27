package com.sriniketh.feature_addhighlight

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun InputHighlightScreen(
    modifier: Modifier = Modifier,
    viewModel: InputHighlightViewModel = hiltViewModel(),
    bookId: String,
    goBack: () -> Unit
) {
    val inputHighlightScreenState: InputHighlightScreenState by viewModel.screenState.collectAsStateWithLifecycle()
    val editHighlightUiState: EditAndSaveHighlightUiState by viewModel.editHighlightUiState.collectAsStateWithLifecycle()
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                viewModel.onImageCaptured()
            } else {
                goBack()
            }
        }
    )

    when (val screenState = inputHighlightScreenState) {
        is InputHighlightScreenState.CaptureImage -> {
            LaunchedEffect(Unit) {
                cameraLauncher.launch(screenState.imageUri)
            }
        }

        is InputHighlightScreenState.CropImage -> {
            CropImageScreen(
                modifier = modifier,
                imageUri = screenState.imageUri,
                onImageCropped = { viewModel.onImageCropped() }
            )
        }

        InputHighlightScreenState.EditAndSaveHighlight -> {
            EditAndSaveHighlightScreen(
                modifier = modifier,
                uiState = editHighlightUiState,
                updateHighlightText = { highlightText ->
                    viewModel.onHighlightTextUpdated(highlightText)
                },
                saveHighlight = { highlight ->
                    viewModel.onHighlightSaved(bookId, highlight)
                },
                goBack = {
                    goBack()
                }
            )
        }

        InputHighlightScreenState.SaveHighlightSuccessful -> {
            goBack()
        }
    }
}
