package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun CaptureAndCropImageScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureAndCropImageViewModel = hiltViewModel(),
    onImageCaptured: (Uri) -> Unit,
    goBack: () -> Unit
) {
    val captureAndCropImageScreenState: CaptureAndCropImageScreenState by viewModel.screenState.collectAsStateWithLifecycle()
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

    when (val screenState = captureAndCropImageScreenState) {
        is CaptureAndCropImageScreenState.CaptureImage -> {
            LaunchedEffect(Unit) {
                cameraLauncher.launch(screenState.imageUri)
            }
        }

        is CaptureAndCropImageScreenState.CropImage -> {
            CropImageScreen(
                modifier = modifier,
                imageUri = screenState.imageUri,
                onImageCropped = { viewModel.onImageCropped() }
            )
        }

        is CaptureAndCropImageScreenState.ImageCapturedAndCropped -> {
            onImageCaptured(screenState.imageUri)
        }
    }
}
