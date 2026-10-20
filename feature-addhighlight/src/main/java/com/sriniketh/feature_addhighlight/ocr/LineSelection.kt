package com.sriniketh.feature_addhighlight.ocr

internal data class LineSelection(
    val selected: Set<Int> = emptySet(),
    private val drag: Drag? = null,
) {
    internal data class Drag(val anchor: Int, val selecting: Boolean, val before: Set<Int>)

    fun toggle(lines: IntRange): LineSelection =
        if (lines.all { it in selected }) {
            copy(selected = selected - lines)
        } else {
            copy(selected = selected + lines)
        }

    fun startDrag(line: Int): LineSelection =
        copy(drag = Drag(anchor = line, selecting = line !in selected, before = selected))
            .dragTo(line)

    fun dragTo(line: Int): LineSelection {
        val drag = drag ?: return this
        val range = minOf(drag.anchor, line)..maxOf(drag.anchor, line)
        return copy(selected = if (drag.selecting) drag.before + range else drag.before - range)
    }

    fun endDrag(): LineSelection = copy(drag = null)
}
