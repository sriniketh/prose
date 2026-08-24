package com.sriniketh.prose

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun whenMainActivityLaunchesThenBookshelfPageTitleIsDisplayed() {
        composeTestRule.onNodeWithText("Bookshelf").assertIsDisplayed()
    }

    @Test
    fun whenMainActivityLaunchesThenSearchForABookButtonIsDisplayed() {
        composeTestRule.onNodeWithContentDescription("search for a book button").assertIsDisplayed()
    }

    @Test
    fun whenMainActivityLaunchesThenNoOtherScreenContentIsDisplayed() {
        composeTestRule.onNodeWithTag("SearchBookTextField").assertDoesNotExist()
    }
}
