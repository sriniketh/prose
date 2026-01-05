package com.sriniketh.core_design.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

object AnimationConstants {
    const val BOOKSHELF_TO_SEARCH_BOUNDS_TRANSITION_KEY =
        "BOOKSHELF_TO_SEARCH_BOUNDS_TRANSITION_KEY"
}

@Composable
fun Modifier.sharedBoundsTransition(key: String): Modifier {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalAnimatedVisibilityScope.current
    if (sharedTransitionScope != null && animatedVisibilityScope != null) {
        return with(sharedTransitionScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = key),
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
    }
    return this
}
