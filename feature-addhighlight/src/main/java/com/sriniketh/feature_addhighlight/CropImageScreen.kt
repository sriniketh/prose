package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.moyuru.cropify.Cropify
import io.moyuru.cropify.rememberCropifyState

@Composable
internal fun CropImageScreen(
    modifier: Modifier = Modifier,
    imageUri: Uri,
    onImageCropped: () -> Unit
) {
    val context = LocalContext.current
    val cropifyState = rememberCropifyState()
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { cropifyState.crop() },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = stringResource(id = R.string.done_fab_cont_desc)
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { contentPadding ->

        val rotatedBitmap = ImageRotater.getRotatedBitmap(context, imageUri)
        if (rotatedBitmap != null) {
            Cropify(
                modifier = modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                bitmap = rotatedBitmap.asImageBitmap(),
                state = cropifyState,
                onImageCropped = { imageBitmap ->
                    val croppedBitmap = imageBitmap.asAndroidBitmap()
                    context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    onImageCropped()
                }
            )
        } else {
            Cropify(
                modifier = modifier
                    .padding(contentPadding)
                    .fillMaxSize(),
                uri = imageUri,
                state = cropifyState,
                onImageCropped = { imageBitmap ->
                    val croppedBitmap = imageBitmap.asAndroidBitmap()
                    context.contentResolver.openOutputStream(imageUri)?.use { outputStream ->
                        croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    }
                    onImageCropped()
                },
                onFailedToLoadImage = { onImageCropped() }
            )
        }
    }
}
