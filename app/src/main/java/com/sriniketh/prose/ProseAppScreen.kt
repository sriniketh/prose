package com.sriniketh.prose

import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.sriniketh.core_design.ui.LocalAnimatedVisibilityScope
import com.sriniketh.core_design.ui.LocalSharedTransitionScope
import com.sriniketh.feature_addhighlight.CaptureAndCropImageScreen
import com.sriniketh.feature_addhighlight.EditAndSaveHighlightScreen
import com.sriniketh.feature_bookshelf.BOOKSHELF_SHOW_ADDED_MESSAGE
import com.sriniketh.feature_bookshelf.BookshelfScreen
import com.sriniketh.feature_searchbooks.BookInfoScreen
import com.sriniketh.feature_searchbooks.SearchBookScreen
import com.sriniketh.feature_viewhighlights.ViewHighlightsScreen

@Composable
internal fun ProseAppScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Bookshelf
            ) {
                composable<Bookshelf> {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        BookshelfScreen(
                            modifier = modifier,
                            goToSearch = { navController.navigate(Search) },
                            goToHighlight = { bookId ->
                                navController.navigate(ViewHighlights(bookId))
                            }
                        )
                    }
                }
                composable<Search> {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        SearchBookScreen(
                            modifier = modifier,
                            goToBookInfo = { bookId ->
                                navController.navigate(BookInfo(bookId))
                            }
                        )
                    }
                }
                composable<ViewHighlights> { backStackEntry ->
                    val bookId = backStackEntry.toRoute<ViewHighlights>().bookId
                    ViewHighlightsScreen(
                        modifier = modifier,
                        bookId = bookId,
                        goBack = { navController.navigateUp() },
                        goToAddHighlightScreen = {
                            navController.navigate(CaptureAndCropImage(bookId))
                        },
                        goToEditHighlightScreen = { highlightId ->
                            navController.navigate(SaveHighlightFromHighlightId(bookId, highlightId))
                        }
                    )
                }
                composable<CaptureAndCropImage> { backStackEntry ->
                    val bookId = backStackEntry.toRoute<CaptureAndCropImage>().bookId
                    CaptureAndCropImageScreen(
                        modifier = modifier,
                        onImageCaptured = { imageUri ->
                            navController.navigate(SaveHighlightFromUri(bookId, imageUri.toString()))
                        },
                        goBack = {
                            navController.popBackStack(CaptureAndCropImage(bookId), inclusive = true)
                        }
                    )
                }
                composable<SaveHighlightFromUri> { backStackEntry ->
                    val route = backStackEntry.toRoute<SaveHighlightFromUri>()
                    EditAndSaveHighlightScreen(
                        uri = route.uri.toUri(),
                        bookId = route.bookId,
                        goBack = {
                            navController.popBackStack(
                                ViewHighlights(route.bookId),
                                inclusive = false
                            )
                        }
                    )
                }
                composable<SaveHighlightFromHighlightId> { backStackEntry ->
                    val route = backStackEntry.toRoute<SaveHighlightFromHighlightId>()
                    EditAndSaveHighlightScreen(
                        highlightId = route.highlightId,
                        bookId = route.bookId,
                        goBack = {
                            navController.popBackStack(
                                ViewHighlights(route.bookId),
                                inclusive = false
                            )
                        }
                    )
                }
                composable<BookInfo> { backStackEntry ->
                    BookInfoScreen(
                        modifier = modifier,
                        bookId = backStackEntry.toRoute<BookInfo>().bookId,
                        goBack = { navController.navigateUp() },
                        onBookAddedToShelf = {
                            navController.getBackStackEntry(Bookshelf)
                                .savedStateHandle[BOOKSHELF_SHOW_ADDED_MESSAGE] = true
                            navController.popBackStack(Bookshelf, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
