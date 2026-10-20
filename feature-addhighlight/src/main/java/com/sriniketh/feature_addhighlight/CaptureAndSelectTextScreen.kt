package com.sriniketh.feature_addhighlight

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sriniketh.core_design.ui.components.NavigationBack
import com.sriniketh.feature_addhighlight.ocr.LineSelection
import com.sriniketh.feature_addhighlight.ocr.OcrPage
import kotlin.math.roundToInt

@Composable
fun CaptureAndSelectTextScreen(
    modifier: Modifier = Modifier,
    viewModel: CaptureAndSelectTextViewModel = hiltViewModel(),
    onTextSelected: (String) -> Unit,
    goBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { photoTaken -> viewModel.onCameraClosed(photoTaken) }
    )

    when (val state = uiState) {
        is CaptureAndSelectTextUiState.OpenCamera -> LaunchedEffect(state) {
            cameraLauncher.launch(state.photoUri)
            viewModel.onCameraOpened()
        }

        CaptureAndSelectTextUiState.Abandoned -> LaunchedEffect(Unit) { goBack() }
        else -> Unit
    }

    SelectText(
        modifier = modifier,
        uiState = uiState,
        onToggleBlock = viewModel::toggleBlock,
        onStartDrag = viewModel::startDrag,
        onDragTo = viewModel::dragTo,
        onEndDrag = viewModel::endDrag,
        onRetake = viewModel::retake,
        onContinue = onTextSelected,
        goBack = goBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectText(
    uiState: CaptureAndSelectTextUiState,
    onToggleBlock: (IntRange) -> Unit,
    onStartDrag: (Int) -> Unit,
    onDragTo: (Int) -> Unit,
    onEndDrag: () -> Unit,
    onRetake: () -> Unit,
    onContinue: (String) -> Unit,
    goBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("SelectTextScreen"),
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.select_text_title)) },
                navigationIcon = { NavigationBack { goBack() } },
                actions = {
                    if (uiState is CaptureAndSelectTextUiState.Selecting) {
                        TextButton(onClick = onRetake) {
                            Text(text = stringResource(id = R.string.retake_button_label))
                        }
                    }
                }
            )
        },
        bottomBar = {
            SelectTextBottomBar(uiState = uiState, onRetake = onRetake, onContinue = onContinue)
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .background(Color.Black)
        ) {
            when (uiState) {
                is CaptureAndSelectTextUiState.Selecting -> PhotoTextOverlay(
                    photo = uiState.photo,
                    page = uiState.page,
                    selection = uiState.selection,
                    onToggleBlock = onToggleBlock,
                    onStartDrag = onStartDrag,
                    onDragTo = onDragTo,
                    onEndDrag = onEndDrag
                )

                is CaptureAndSelectTextUiState.Reading -> uiState.photo?.let { Photo(it) }
                is CaptureAndSelectTextUiState.Failed -> uiState.photo?.let { Photo(it) }
                else -> Unit
            }
            if (uiState is CaptureAndSelectTextUiState.Reading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ReadingTextIndicator")
                )
            }
        }
    }
}

@Composable
private fun SelectTextBottomBar(
    uiState: CaptureAndSelectTextUiState,
    onRetake: () -> Unit,
    onContinue: (String) -> Unit
) {
    val message = when (uiState) {
        is CaptureAndSelectTextUiState.Reading -> stringResource(id = R.string.reading_text_message)
        is CaptureAndSelectTextUiState.Failed -> stringResource(id = uiState.message)
        is CaptureAndSelectTextUiState.Selecting -> uiState.selectedText.ifEmpty {
            stringResource(id = R.string.select_text_hint)
        }

        else -> return
    }
    Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            when (uiState) {
                is CaptureAndSelectTextUiState.Failed -> Button(onClick = onRetake) {
                    Text(text = stringResource(id = R.string.retake_button_label))
                }

                is CaptureAndSelectTextUiState.Selecting -> Button(
                    modifier = Modifier.testTag("ContinueWithSelectionButton"),
                    enabled = uiState.selectedText.isNotBlank(),
                    onClick = { onContinue(uiState.selectedText) }
                ) {
                    Text(text = stringResource(id = R.string.continue_button_label))
                }

                else -> Unit
            }
        }
    }
}

@Composable
private fun Photo(photo: ImageBitmap) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fit = PhotoFit(photo, constraints.maxWidth, constraints.maxHeight)
        Canvas(modifier = Modifier.fillMaxSize()) { drawPhoto(photo, fit) }
    }
}

@Composable
private fun PhotoTextOverlay(
    photo: ImageBitmap,
    page: OcrPage,
    selection: LineSelection,
    onToggleBlock: (IntRange) -> Unit,
    onStartDrag: (Int) -> Unit,
    onDragTo: (Int) -> Unit,
    onEndDrag: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val touchSlop = LocalViewConfiguration.current.touchSlop
    val density = LocalDensity.current
    val highlightColor = MaterialTheme.colorScheme.run {
        if (surface.luminance() < 0.5f) inversePrimary else primary
    }
    val currentOnToggleBlock by rememberUpdatedState(onToggleBlock)
    val currentOnStartDrag by rememberUpdatedState(onStartDrag)
    val currentOnDragTo by rememberUpdatedState(onDragTo)
    val currentOnEndDrag by rememberUpdatedState(onEndDrag)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val fit = remember(photo, constraints) {
            PhotoFit(photo, constraints.maxWidth, constraints.maxHeight)
        }
        val currentFit by rememberUpdatedState(fit)
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("PhotoTextOverlay")
                .pointerInput(page) {
                    detectTapGestures { position ->
                        page.blockAt(currentFit.toPhoto(position), tolerance = touchSlop / currentFit.scale)
                            ?.let(currentOnToggleBlock)
                    }
                }
                .pointerInput(page) {
                    var lastLine: Int? = null
                    detectDragGestures(
                        orientationLock = null,
                        onDragStart = { down, _, _ ->
                            lastLine = page.lineNearest(currentFit.toPhoto(down.position))
                            lastLine?.let(currentOnStartDrag)
                        },
                        onDragEnd = { currentOnEndDrag() },
                        onDragCancel = { currentOnEndDrag() },
                        onDrag = { change, _ ->
                            change.consume()
                            val line = page.lineNearest(currentFit.toPhoto(change.position))
                            if (line != null && line != lastLine) {
                                lastLine = line
                                currentOnDragTo(line)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                        }
                    )
                }
        ) {
            drawPhoto(photo, fit)
            page.lines.forEachIndexed { index, line ->
                drawPath(
                    path = fit.toView(line.corners),
                    color = highlightColor.copy(alpha = if (index in selection.selected) 0.4f else 0.12f)
                )
            }
        }
        page.blocks.forEachIndexed { index, block ->
            val bounds = fit.toView(page.blockBounds[index])
            val blockText = page.textOf(block.toSet())
            Box(
                modifier = Modifier
                    .offset { IntOffset(bounds.left.roundToInt(), bounds.top.roundToInt()) }
                    .size(with(density) { bounds.width.toDp() }, with(density) { bounds.height.toDp() })
                    .clearAndSetSemantics {
                        contentDescription = blockText
                        selected = block.all { it in selection.selected }
                        onClick { onToggleBlock(block); true }
                    }
            )
        }
    }
}

private fun DrawScope.drawPhoto(
    photo: ImageBitmap,
    fit: PhotoFit
) {
    drawImage(
        image = photo,
        dstOffset = IntOffset(fit.offset.x.roundToInt(), fit.offset.y.roundToInt()),
        dstSize = IntSize(
            (photo.width * fit.scale).roundToInt(),
            (photo.height * fit.scale).roundToInt()
        )
    )
}

private class PhotoFit(photo: ImageBitmap, viewWidth: Int, viewHeight: Int) {
    val scale: Float = minOf(viewWidth.toFloat() / photo.width, viewHeight.toFloat() / photo.height)
    val offset: Offset = Offset(
        x = (viewWidth - photo.width * scale) / 2f,
        y = (viewHeight - photo.height * scale) / 2f
    )

    fun toPhoto(point: Offset): Offset = (point - offset) / scale

    fun toView(rect: Rect): Rect = Rect(
        topLeft = rect.topLeft * scale + offset,
        bottomRight = rect.bottomRight * scale + offset
    )

    fun toView(corners: List<Offset>): Path = Path().apply {
        corners.map { it * scale + offset }.forEachIndexed { index, corner ->
            if (index == 0) moveTo(corner.x, corner.y) else lineTo(corner.x, corner.y)
        }
        close()
    }
}
