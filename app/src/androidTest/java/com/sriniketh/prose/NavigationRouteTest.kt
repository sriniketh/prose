package com.sriniketh.prose

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationRouteTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavHostController

    private fun setUpNavGraph() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current).apply {
                navigatorProvider.addNavigator(ComposeNavigator())
            }
            NavHost(navController = navController, startDestination = Bookshelf) {
                composable<Bookshelf> { DestinationMarker("bookshelf") }
                composable<Search> { DestinationMarker("search") }
                composable<BookInfo> { DestinationMarker("book_info") }
                composable<ViewHighlights> { DestinationMarker("view_highlights") }
                composable<CaptureAndCropImage> { DestinationMarker("capture_and_crop_image") }
                composable<SaveHighlightFromUri> { DestinationMarker("save_highlight_from_uri") }
                composable<SaveHighlightFromHighlightId> { DestinationMarker("save_highlight_from_highlight_id") }
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun navigateTo(route: Any) {
        composeTestRule.runOnUiThread { navController.navigate(route) }
        composeTestRule.waitForIdle()
    }

    @Test
    fun startDestinationIsBookshelf() {
        setUpNavGraph()

        composeTestRule.onNodeWithTag("bookshelf").assertIsDisplayed()
        assertEquals(
            true,
            navController.currentBackStackEntry!!.destination.hasRoute(Bookshelf::class)
        )
    }

    @Test
    fun navigateToSearchReachesSearchDestination() {
        setUpNavGraph()

        navigateTo(Search)

        assertEquals(
            true,
            navController.currentBackStackEntry!!.destination.hasRoute(Search::class)
        )
    }

    @Test
    fun navigateToBookInfoDeliversBookId() {
        setUpNavGraph()

        navigateTo(BookInfo("book-info-42"))

        assertEquals(
            "book-info-42",
            navController.currentBackStackEntry!!.toRoute<BookInfo>().bookId
        )
    }

    @Test
    fun navigateToViewHighlightsDeliversBookId() {
        setUpNavGraph()

        navigateTo(ViewHighlights("book-99"))

        assertEquals(
            "book-99",
            navController.currentBackStackEntry!!.toRoute<ViewHighlights>().bookId
        )
    }

    @Test
    fun navigateToCaptureAndCropImageDeliversBookId() {
        setUpNavGraph()

        navigateTo(CaptureAndCropImage("book-7"))

        assertEquals(
            "book-7",
            navController.currentBackStackEntry!!.toRoute<CaptureAndCropImage>().bookId
        )
    }

    @Test
    fun navigateToSaveHighlightFromHighlightIdDeliversBothArgs() {
        setUpNavGraph()

        navigateTo(SaveHighlightFromHighlightId("book-1", "highlight-2"))

        val route = navController.currentBackStackEntry!!.toRoute<SaveHighlightFromHighlightId>()
        assertEquals("book-1", route.bookId)
        assertEquals("highlight-2", route.highlightId)
    }

    @Test
    fun navigateToSaveHighlightFromUriPreservesUriWithReservedCharacters() {
        setUpNavGraph()

        val uri = "content://com.sriniketh.prose.provider/cache/temp image 1.jpg?x=1&y=2"
        navigateTo(SaveHighlightFromUri("book-3", uri))

        val route = navController.currentBackStackEntry!!.toRoute<SaveHighlightFromUri>()
        assertEquals("book-3", route.bookId)
        assertEquals(uri, route.uri)
    }
}

@androidx.compose.runtime.Composable
private fun DestinationMarker(tag: String) {
    Text(text = tag, modifier = Modifier.testTag(tag))
}
