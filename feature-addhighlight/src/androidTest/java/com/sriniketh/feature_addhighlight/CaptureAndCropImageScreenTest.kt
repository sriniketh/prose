package com.sriniketh.feature_addhighlight

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CaptureAndCropImageScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val mockUri = Uri.parse("content://test.jpg")

	@Test
	fun whenCropImageStateIsDisplayedThenCropScreenIsShown() {
		val screenState = CaptureAndCropImageScreenState.CropImage(mockUri)

		composeTestRule.setContent {
			AppTheme {
				CaptureAndCropImageScreenContent(
					screenState = screenState,
					onImageCropped = {}
				)
			}
		}

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithTag("AddHighlightCropImageScreen").assertIsDisplayed()
	}

	@Test
	fun whenImageCapturedAndCroppedStateThenNavigationOccurs() {
		val screenState = CaptureAndCropImageScreenState.ImageCapturedAndCropped(mockUri)
		var imageCapturedCalled = false

		composeTestRule.setContent {
			AppTheme {
				CaptureAndCropImageScreenContent(
					screenState = screenState,
					onImageCropped = { imageCapturedCalled = true }
				)
			}
		}

		composeTestRule.waitForIdle()
		assertTrue(imageCapturedCalled)
	}

	@Composable
	private fun CaptureAndCropImageScreenContent(
		screenState: CaptureAndCropImageScreenState,
		onImageCropped: () -> Unit
	) {
		when (screenState) {
			is CaptureAndCropImageScreenState.CaptureImage -> {}

			is CaptureAndCropImageScreenState.CropImage -> {
				CropImageScreen(
					imageUri = screenState.imageUri,
					onImageCropped = onImageCropped
				)
			}

			is CaptureAndCropImageScreenState.ImageCapturedAndCropped -> {
				LaunchedEffect(Unit) {
					onImageCropped()
				}
			}
		}
	}
}
