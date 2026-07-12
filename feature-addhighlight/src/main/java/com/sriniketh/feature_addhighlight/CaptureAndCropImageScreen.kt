package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch

@Composable
fun CaptureAndCropImageScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureAndCropImageViewModel = hiltViewModel(),
    onImageCaptured: (Uri) -> Unit,
    goBack: () -> Unit
) {
    val captureAndCropImageScreenState: CaptureAndCropImageScreenState by viewModel.screenState.collectAsStateWithLifecycle()
    val rotatedBitmap: Bitmap? by viewModel.rotatedBitmap.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

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

    LaunchedEffect(Unit) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is CaptureAndCropImageEffect.ShowMessage -> scope.launch {
                        snackbarHostState.showSnackbar(resources.getString(effect.messageRes))
                    }
                }
            }
        }
    }

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
                rotatedBitmap = rotatedBitmap,
                onImageCropped = { croppedBitmap -> viewModel.onImageCropped(croppedBitmap) },
                onImageLoadFailed = { viewModel.onImageLoadFailed() },
                snackbarHostState = snackbarHostState
            )
        }

        is CaptureAndCropImageScreenState.ImageCapturedAndCropped -> {
            onImageCaptured(screenState.imageUri)
        }
    }
}
