package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sriniketh.core_design.ui.theme.AppTheme
import com.sriniketh.feature_addhighlight.fakes.FakeFileSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class CaptureAndCropImageScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val mockUri = Uri.parse("content://test.jpg")

	private fun createViewModel(fileSource: FakeFileSource = FakeFileSource()): CaptureAndCropImageViewModel =
		CaptureAndCropImageViewModel(
			fileSource = fileSource,
			savedStateHandle = SavedStateHandle()
		)

	private fun writeValidImageUri(): Uri {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val bitmap = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888)
		bitmap.eraseColor(Color.BLUE)
		val file = File(context.cacheDir, "capture_crop_test.png")
		FileOutputStream(file).use { outputStream ->
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
		}
		return Uri.fromFile(file)
	}

	@Test
	fun whenScreenStateIsCropImageThenRealScreenRendersCropImageScreen() {
		val viewModel = createViewModel(FakeFileSource(uriToReturn = writeValidImageUri()))
		viewModel.onImageCaptured()

		composeTestRule.setContent {
			AppTheme {
				CaptureAndCropImageScreen(
					viewModel = viewModel,
					onImageCaptured = {},
					goBack = {}
				)
			}
		}

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithTag("AddHighlightCropImageScreen").assertIsDisplayed()
	}

	@Test
	fun whenScreenStateIsImageCapturedAndCroppedThenRealScreenInvokesOnImageCaptured() {
		val viewModel = createViewModel()
		viewModel.onImageCaptured()
		viewModel.onImageCropped()
		var capturedUri: Uri? = null

		composeTestRule.setContent {
			AppTheme {
				CaptureAndCropImageScreen(
					viewModel = viewModel,
					onImageCaptured = { capturedUri = it },
					goBack = {}
				)
			}
		}

		composeTestRule.waitForIdle()
		assertEquals(
			(viewModel.screenState.value as CaptureAndCropImageScreenState.ImageCapturedAndCropped).imageUri,
			capturedUri
		)
	}

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
