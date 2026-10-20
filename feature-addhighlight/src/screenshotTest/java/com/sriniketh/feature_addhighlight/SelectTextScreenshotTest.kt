package com.sriniketh.feature_addhighlight

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.android.tools.screenshot.PreviewTest
import com.sriniketh.core_design.ui.preview.ScreenSizePreviews
import com.sriniketh.core_design.ui.preview.ScreenStatePreviews
import com.sriniketh.core_design.ui.preview.ScreenVariantPreviews
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.feature_addhighlight.ocr.LineSelection
import com.sriniketh.feature_addhighlight.ocr.OcrLine
import com.sriniketh.feature_addhighlight.ocr.OcrPage

private val lineTexts = listOf(
    listOf(
        "It is a truth universally acknowledged, that a",
        "single man in possession of a good fortune,",
        "must be in want of a wife."
    ),
    listOf(
        "However little known the feelings or views of",
        "such a man may be on his first entering a",
        "neighbourhood, this truth is so well fixed."
    )
)

private val page = OcrPage(
    width = 1200,
    height = 1600,
    lines = lineTexts.flatMapIndexed { block, lines ->
        lines.mapIndexed { index, text ->
            val top = 200f + block * 320f + index * 80f
            val right = if (index == lines.lastIndex) 760f else 1080f
            OcrLine(
                text = text,
                blockIndex = block,
                corners = listOf(
                    Offset(120f, top),
                    Offset(right, top),
                    Offset(right, top + 48f),
                    Offset(120f, top + 48f)
                )
            )
        }
    }
)

private fun pagePhoto(): ImageBitmap {
    val photo = ImageBitmap(page.width, page.height)
    val canvas = Canvas(photo)
    canvas.drawRect(0f, 0f, page.width.toFloat(), page.height.toFloat(), Paint().apply {
        color = Color(0xFFF6F1E4)
    })
    val ink = Paint().apply { color = Color(0xFF55504A) }
    page.lines.forEach { line ->
        val bounds = line.bounds
        canvas.drawRect(bounds.left, bounds.top + 12f, bounds.right, bounds.bottom - 12f, ink)
    }
    return photo
}

@Composable
private fun SelectTextPreview(uiState: CaptureAndSelectTextUiState) {
    AppTheme {
        SelectText(
            uiState = uiState,
            onToggleBlock = {},
            onStartDrag = {},
            onDragTo = {},
            onEndDrag = {},
            onRetake = {},
            onContinue = {},
            goBack = {}
        )
    }
}

@PreviewTest
@ScreenSizePreviews
@ScreenVariantPreviews
@Composable
fun SelectTextWithSelection() {
    SelectTextPreview(
        CaptureAndSelectTextUiState.Selecting(
            photo = pagePhoto(),
            page = page,
            selection = LineSelection(selected = setOf(1, 2, 3))
        )
    )
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SelectTextNothingSelected() {
    SelectTextPreview(CaptureAndSelectTextUiState.Selecting(photo = pagePhoto(), page = page))
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SelectTextReading() {
    SelectTextPreview(CaptureAndSelectTextUiState.Reading(photo = pagePhoto()))
}

@PreviewTest
@ScreenStatePreviews
@Composable
fun SelectTextNoTextFound() {
    SelectTextPreview(
        CaptureAndSelectTextUiState.Failed(photo = pagePhoto(), message = R.string.no_text_found_message)
    )
}
