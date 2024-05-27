package com.sriniketh.prose

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sriniketh.feature_addhighlight.InputHighlightScreen
import com.sriniketh.feature_bookshelf.BookshelfScreen
import com.sriniketh.feature_searchbooks.BookInfoScreen
import com.sriniketh.feature_searchbooks.SearchBookScreen
import com.sriniketh.feature_viewhighlights.ViewHighlightsScreen

@Composable
internal fun ProseAppScreen(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.BOOKSHELF.route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(250)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Left,
                animationSpec = tween(250)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(250)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Companion.Right,
                animationSpec = tween(250)
            )
        }
    ) {
        composable(Screen.BOOKSHELF.route) {
            BookshelfScreen(
                modifier = modifier,
                goToSearch = { navController.navigate(Screen.SEARCH.route) },
                goToHighlight = { bookId ->
                    navController.navigate("${Screen.VIEWHIGHLIGHTS.route}/$bookId")
                }
            )
        }
        composable(Screen.SEARCH.route) {
            SearchBookScreen(
                modifier = modifier,
                goToBookInfo = { bookId ->
                    navController.navigate("${Screen.BOOKINFO.route}/$bookId")
                }
            )
        }
        composable(
            route = "${Screen.VIEWHIGHLIGHTS.route}/{${Screen.VIEWHIGHLIGHTS.argBookId}}",
            arguments = listOf(navArgument(Screen.VIEWHIGHLIGHTS.argBookId) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString(Screen.VIEWHIGHLIGHTS.argBookId)
                .orEmpty()
            ViewHighlightsScreen(
                modifier = modifier,
                bookId = bookId,
                goBack = { navController.navigateUp() },
                goToInputHighlightScreen = {
                    navController.navigate("${Screen.INPUTHIGHLIGHT.route}/$bookId")
                }
            )
        }
        composable(
            route = "${Screen.INPUTHIGHLIGHT.route}/{${Screen.INPUTHIGHLIGHT.argBookId}}",
            arguments = listOf(navArgument(Screen.INPUTHIGHLIGHT.argBookId) {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString(Screen.INPUTHIGHLIGHT.argBookId)
                .orEmpty()
            InputHighlightScreen(
                modifier = modifier,
                bookId = backStackEntry.arguments?.getString(Screen.INPUTHIGHLIGHT.argBookId)
                    .orEmpty(),
                goBack = {
                    navController.popBackStack(
                        "${Screen.INPUTHIGHLIGHT.route}/$bookId",
                        inclusive = true
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
                goBack = { navController.navigateUp() }
            )
        }
    }
}
