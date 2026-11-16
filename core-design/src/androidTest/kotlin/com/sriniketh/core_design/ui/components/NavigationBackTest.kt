package com.sriniketh.core_design.ui.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.R
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationBackTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val backContentDescription
        get() = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.nav_back_arrow_cont_desc)

    @Test
    fun whenNavigationBackIsDisplayedThenBackArrowContentDescriptionIsShown() {
        composeTestRule.setContent {
            AppTheme {
                NavigationBack(action = {})
            }
        }

        composeTestRule.onNodeWithContentDescription(backContentDescription).assertIsDisplayed()
    }

    @Test
    fun whenNavigationBackIsClickedThenActionIsInvoked() {
        var actionCalled = false

        composeTestRule.setContent {
            AppTheme {
                NavigationBack(action = { actionCalled = true })
            }
        }

        composeTestRule.onNodeWithContentDescription(backContentDescription).performClick()
        assertTrue(actionCalled)
    }
}
