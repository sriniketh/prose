package com.sriniketh.feature_addhighlight.ocr

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs

data class OcrLine(
    val text: String,
    val blockIndex: Int,
    val corners: List<Offset>,
) {
    val bounds: Rect = Rect(
        left = corners.minOf { it.x },
        top = corners.minOf { it.y },
        right = corners.maxOf { it.x },
        bottom = corners.maxOf { it.y },
    )
}

data class OcrPage(
    val width: Int,
    val height: Int,
    val lines: List<OcrLine>,
) {
    val blocks: List<IntRange> = lines.indices
        .groupBy { lines[it].blockIndex }
        .values
        .map { it.first()..it.last() }

    val blockBounds: List<Rect> = blocks.map { block ->
        block.map { lines[it].bounds }.reduce { union, bounds ->
            Rect(
                left = minOf(union.left, bounds.left),
                top = minOf(union.top, bounds.top),
                right = maxOf(union.right, bounds.right),
                bottom = maxOf(union.bottom, bounds.bottom),
            )
        }
    }

    fun blockAt(point: Offset, tolerance: Float): IntRange? =
        blocks.indices
            .filter { blockBounds[it].inflate(tolerance).contains(point) }
            .minByOrNull { blockBounds[it].width * blockBounds[it].height }
            ?.let(blocks::get)

    fun lineNearest(point: Offset): Int? =
        lines.indices.minWithOrNull(
            compareBy<Int> { lines[it].bounds.verticalDistanceTo(point) }
                .thenBy { lines[it].bounds.horizontalDistanceTo(point) }
                .thenBy { abs(lines[it].bounds.center.y - point.y) }
        )

    fun textOf(selectedLines: Set<Int>): String =
        selectedLines.sorted()
            .groupConsecutiveBy { lines[it].blockIndex }
            .map { block -> block.map { lines[it].text }.reduce { text, line -> join(text, line, " ") } }
            .reduceOrNull { text, block ->
                join(text, block, if (text.last() in SENTENCE_ENDINGS) "\n" else " ")
            }
            .orEmpty()
}

private const val SENTENCE_ENDINGS = ".!?:;…\"'”’)"

private fun join(previous: String, next: String, separator: String): String {
    val endsWithHyphenatedWord = previous.length > 1 &&
        previous.last() == '-' &&
        previous[previous.lastIndex - 1].isLetter()
    return if (endsWithHyphenatedWord && next.firstOrNull()?.isLowerCase() == true) {
        previous.dropLast(1) + next
    } else {
        previous + separator + next
    }
}

private fun Rect.verticalDistanceTo(point: Offset): Float = maxOf(top - point.y, 0f, point.y - bottom)

private fun Rect.horizontalDistanceTo(point: Offset): Float = maxOf(left - point.x, 0f, point.x - right)

private fun List<Int>.groupConsecutiveBy(key: (Int) -> Int): List<List<Int>> =
    fold(mutableListOf<MutableList<Int>>()) { groups, line ->
        val current = groups.lastOrNull()
        if (current != null && key(current.last()) == key(line)) {
            current.add(line)
        } else {
            groups.add(mutableListOf(line))
        }
        groups
    }
