package com.sriniketh.feature_addhighlight.ocr

import org.junit.Assert.assertEquals
import org.junit.Test

class LineSelectionTest {

    @Test
    fun `when an unselected block is toggled then all its lines are selected`() {
        val selection = LineSelection().toggle(2..4)

        assertEquals(setOf(2, 3, 4), selection.selected)
    }

    @Test
    fun `when a partly selected block is toggled then all its lines are selected`() {
        val selection = LineSelection(selected = setOf(3)).toggle(2..4)

        assertEquals(setOf(2, 3, 4), selection.selected)
    }

    @Test
    fun `when a fully selected block is toggled then its lines are deselected and others are kept`() {
        val selection = LineSelection(selected = setOf(0, 2, 3, 4)).toggle(2..4)

        assertEquals(setOf(0), selection.selected)
    }

    @Test
    fun `when dragging from an unselected line then every line between anchor and finger is selected`() {
        val selection = LineSelection(selected = setOf(0)).startDrag(3).dragTo(6)

        assertEquals(setOf(0, 3, 4, 5, 6), selection.selected)
    }

    @Test
    fun `when dragging upwards then the range runs from the finger to the anchor`() {
        val selection = LineSelection().startDrag(5).dragTo(2)

        assertEquals(setOf(2, 3, 4, 5), selection.selected)
    }

    @Test
    fun `when dragging back towards the anchor then the selection shrinks to the earlier state`() {
        val selection = LineSelection(selected = setOf(8)).startDrag(3).dragTo(8).dragTo(4)

        assertEquals(setOf(3, 4, 8), selection.selected)
    }

    @Test
    fun `when dragging from a selected line then lines in the range are deselected`() {
        val selection = LineSelection(selected = setOf(1, 2, 3, 4, 5)).startDrag(2).dragTo(4)

        assertEquals(setOf(1, 5), selection.selected)
    }

    @Test
    fun `when a drag ends then a later drag starts from the new selection`() {
        val selection = LineSelection()
            .startDrag(0).dragTo(2).endDrag()
            .startDrag(5).dragTo(4)

        assertEquals(setOf(0, 1, 2, 4, 5), selection.selected)
    }

    @Test
    fun `when dragging without a started drag then the selection is unchanged`() {
        val selection = LineSelection(selected = setOf(1)).dragTo(4)

        assertEquals(setOf(1), selection.selected)
    }
}
