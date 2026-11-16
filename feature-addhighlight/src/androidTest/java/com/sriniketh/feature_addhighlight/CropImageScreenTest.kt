package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CropImageScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val mockUri = Uri.parse("content://test.jpg")

	@Test
	fun whenScreenIsDisplayedThenScaffoldIsShown() {
		composeTestRule.setContent {
			AppTheme {
				CropImageScreen(
					imageUri = mockUri,
					onImageCropped = {}
				)
			}
		}

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithTag("AddHighlightCropImageScreen").assertIsDisplayed()
	}

	@Test
	fun whenScreenIsDisplayedThenCropFabIsShownWithCorrectContentDescription() {
		composeTestRule.setContent {
			AppTheme {
				CropImageScreen(
					imageUri = mockUri,
					onImageCropped = {}
				)
			}
		}

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithContentDescription("Done cropping button").assertIsDisplayed()
	}
}
