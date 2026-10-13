package com.sriniketh.core_design.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.BrushPainter
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlaceholderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenGradientPlaceholderIsCreatedThenItReturnsABrushPainterWithUnspecifiedIntrinsicSize() {
        var placeholder: BrushPainter? = null

        composeTestRule.setContent {
            AppTheme {
                placeholder = gradientPlaceholder()
            }
        }

        composeTestRule.runOnIdle {
            assertNotNull(placeholder)
            assertEquals(Size.Unspecified, placeholder!!.intrinsicSize)
        }
    }
}
