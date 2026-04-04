package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.moyuru.cropify.Cropify
import io.moyuru.cropify.rememberCropifyState

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        modifier = modifier.testTag("AddHighlightCropImageScreen"),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { cropifyState.crop() },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        painter = painterResource(com.sriniketh.core_design.R.drawable.ic_done),
                        contentDescription = stringResource(id = R.string.done_fab_cont_desc)
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.select_fab_text),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
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
