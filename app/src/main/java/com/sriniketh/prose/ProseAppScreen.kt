package com.sriniketh.prose

import android.net.Uri
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sriniketh.core_design.ui.LocalAnimatedVisibilityScope
import com.sriniketh.core_design.ui.LocalSharedTransitionScope
import com.sriniketh.feature_addhighlight.CaptureAndSelectTextScreen
import com.sriniketh.feature_addhighlight.EditAndSaveHighlightScreen
import com.sriniketh.feature_addhighlight.EditAndSaveNewHighlightScreen
import com.sriniketh.feature_bookshelf.BOOKSHELF_SHOW_ADDED_MESSAGE
import com.sriniketh.feature_bookshelf.BookshelfScreen
import com.sriniketh.feature_searchbooks.BookInfoScreen
import com.sriniketh.feature_searchbooks.SearchBookScreen
import com.sriniketh.feature_viewhighlights.ViewHighlightsScreen

@Composable
internal fun ProseAppScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    SharedTransitionLayout {
        CompositionLocalProvider(LocalSharedTransitionScope provides this) {
            NavHost(
                navController = navController,
                startDestination = Screen.BOOKSHELF.route
            ) {
                composable(Screen.BOOKSHELF.route) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        BookshelfScreen(
                            modifier = modifier,
                            goToSearch = { navController.navigate(Screen.SEARCH.route) },
                            goToHighlight = { bookId ->
                                navController.navigate("${Screen.VIEWHIGHLIGHTS.route}/$bookId")
                            }
                        )
                    }
                }
                composable(Screen.SEARCH.route) {
                    CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                        SearchBookScreen(
                            modifier = modifier,
                            goToBookInfo = { bookId ->
                                navController.navigate("${Screen.BOOKINFO.route}/$bookId")
                            }
                        )
                    }
                }
                composable(
                    route = "${Screen.VIEWHIGHLIGHTS.route}/{${Screen.VIEWHIGHLIGHTS.argBookId}}",
                    arguments = listOf(navArgument(Screen.VIEWHIGHLIGHTS.argBookId) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val bookId =
                        backStackEntry.arguments?.getString(Screen.VIEWHIGHLIGHTS.argBookId)
                            .orEmpty()
                    ViewHighlightsScreen(
                        modifier = modifier,
                        bookId = bookId,
                        goBack = { navController.navigateUp() },
                        goToAddHighlightScreen = {
                            navController.navigate("${Screen.CAPTUREANDSELECTTEXT.route}/$bookId")
                        },
                        goToEditHighlightScreen = { highlightId ->
                            navController.navigate("${Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.route}/$bookId/$highlightId")
                        }
                    )
                }
                composable(
                    route = "${Screen.CAPTUREANDSELECTTEXT.route}/{${Screen.CAPTUREANDSELECTTEXT.argBookId}}",
                    arguments = listOf(navArgument(Screen.CAPTUREANDSELECTTEXT.argBookId) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val bookId =
                        backStackEntry.arguments?.getString(Screen.CAPTUREANDSELECTTEXT.argBookId)
                            .orEmpty()
                    CaptureAndSelectTextScreen(
                        modifier = modifier,
                        onTextSelected = { highlightText ->
                            if (backStackEntry.lifecycle.currentState == Lifecycle.State.RESUMED) {
                                val encodedText = Uri.encode(highlightText)
                                navController.navigate("${Screen.SAVEHIGHLIGHT_FROMTEXT.route}/$bookId/$encodedText")
                            }
                        },
                        goBack = {
                            navController.popBackStack(
                                "${Screen.CAPTUREANDSELECTTEXT.route}/$bookId",
                                inclusive = true
                            )
                        }
                    )
                }
                composable(
                    route = "${Screen.SAVEHIGHLIGHT_FROMTEXT.route}/{${Screen.SAVEHIGHLIGHT_FROMTEXT.argBookId}}/{${Screen.SAVEHIGHLIGHT_FROMTEXT.argText}}",
                    arguments = listOf(
                        navArgument(Screen.SAVEHIGHLIGHT_FROMTEXT.argBookId) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.SAVEHIGHLIGHT_FROMTEXT.argText) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val bookId =
                        backStackEntry.arguments?.getString(Screen.SAVEHIGHLIGHT_FROMTEXT.argBookId)
                            .orEmpty()
                    val highlightText =
                        backStackEntry.arguments?.getString(Screen.SAVEHIGHLIGHT_FROMTEXT.argText)
                            .orEmpty()
                    EditAndSaveNewHighlightScreen(
                        highlightText = highlightText,
                        bookId = bookId,
                        goBack = {
                            navController.popBackStack(
                                "${Screen.VIEWHIGHLIGHTS.route}/$bookId",
                                inclusive = false
                            )
                        }
                    )
                }
                composable(
                    route = "${Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.route}/{${Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argBookId}}/{${Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argHighlightId}}",
                    arguments = listOf(
                        navArgument(Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argBookId) {
                            type = NavType.StringType
                        },
                        navArgument(Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argHighlightId) {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val bookId =
                        backStackEntry.arguments?.getString(Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argBookId)
                            .orEmpty()
                    val highlightId =
                        backStackEntry.arguments?.getString(Screen.SAVEHIGHLIGHT_FROMHIGHLIGHTID.argHighlightId)
                            .orEmpty()
                    EditAndSaveHighlightScreen(
                        highlightId = highlightId,
                        bookId = bookId,
                        goBack = {
                            navController.popBackStack(
                                "${Screen.VIEWHIGHLIGHTS.route}/$bookId",
                                inclusive = false
                            )
                        }
                    )
                }
                composable(
                    route = "${Screen.BOOKINFO.route}/{${Screen.BOOKINFO.argBookId}}",
                    arguments = listOf(navArgument(Screen.BOOKINFO.argBookId) {
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    BookInfoScreen(
                        modifier = modifier,
                        bookId = backStackEntry.arguments?.getString(Screen.BOOKINFO.argBookId)
                            .orEmpty(),
                        goBack = { navController.navigateUp() },
                        onBookAddedToShelf = {
                            navController.getBackStackEntry(Screen.BOOKSHELF.route)
                                .savedStateHandle[BOOKSHELF_SHOW_ADDED_MESSAGE] = true
                            navController.popBackStack(Screen.BOOKSHELF.route, inclusive = false)
                        }
                    )
                }
            }
        }
    }
}
