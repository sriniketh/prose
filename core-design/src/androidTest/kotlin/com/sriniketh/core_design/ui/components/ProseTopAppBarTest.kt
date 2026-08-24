package com.sriniketh.core_design.ui.components

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.R
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class ProseTopAppBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenProseTopAppBarIsDisplayedThenTitleContentIsShown() {
        composeTestRule.setContent {
            AppTheme {
                ProseTopAppBar(
                    title = { Text("Test Title") }
                )
            }
        }

        composeTestRule.onNodeWithText("Test Title").assertIsDisplayed()
    }

    @Test
    fun whenProseTopAppBarHasNavigationIconAndActionsThenBothAreDisplayed() {
        composeTestRule.setContent {
            AppTheme {
                ProseTopAppBar(
                    title = { Text("Test Title") },
                    navigationIcon = {
                        NavigationBack(action = {})
                    },
                    actions = {
                        IconButton(onClick = {}) {
                            Icon(
                                painter = painterResource(R.drawable.ic_share),
                                contentDescription = "Share"
                            )
                        }
                    }
                )
            }
        }

        val backContentDescription = ApplicationProvider.getApplicationContext<Context>()
            .getString(R.string.nav_back_arrow_cont_desc)
        composeTestRule.onNodeWithContentDescription(backContentDescription).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Share").assertIsDisplayed()
    }
}
