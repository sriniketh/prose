package com.sriniketh.feature_addhighlight

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sriniketh.core_design.ui.theme.AppTheme
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class CropImageScreenTest {

	@get:Rule
	val composeTestRule = createComposeRule()

	private val mockUri = Uri.parse("content://test.jpg")

	private fun writeValidImageFile(fileName: String): Uri {
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
		bitmap.eraseColor(Color.GREEN)
		val file = File(context.cacheDir, fileName)
		FileOutputStream(file).use { outputStream ->
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
		}
		return Uri.fromFile(file)
	}

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

	@Test
	fun whenImageFailsToLoadThenOnImageCroppedIsInvoked() {
		var onImageCroppedCalled = false

		composeTestRule.setContent {
			AppTheme {
				CropImageScreen(
					imageUri = mockUri,
					onImageCropped = { onImageCroppedCalled = true }
				)
			}
		}

		composeTestRule.waitUntil(timeoutMillis = 10_000) { onImageCroppedCalled }
		assertTrue(onImageCroppedCalled)
	}

	@Test
	fun whenImageLoadsSuccessfullyAndCropFabIsClickedThenCroppedBitmapIsWrittenBackToUri() {
		val imageUri = writeValidImageFile("crop_success_test.png")
		var onImageCroppedCalled = false

		composeTestRule.setContent {
			AppTheme {
				CropImageScreen(
					imageUri = imageUri,
					onImageCropped = { onImageCroppedCalled = true }
				)
			}
		}

		composeTestRule.waitForIdle()
		composeTestRule.onNodeWithContentDescription("Done cropping button").performClick()
		composeTestRule.waitUntil(timeoutMillis = 10_000) { onImageCroppedCalled }

		assertTrue(onImageCroppedCalled)
		val context = InstrumentationRegistry.getInstrumentation().targetContext
		val croppedBitmap = context.contentResolver.openInputStream(imageUri)?.use {
			BitmapFactory.decodeStream(it)
		}
		assertNotNull(croppedBitmap)
	}
}
