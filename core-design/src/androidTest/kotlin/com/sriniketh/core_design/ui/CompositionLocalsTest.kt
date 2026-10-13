package com.sriniketh.core_design.ui

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompositionLocalsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenNoProviderSuppliesAValueThenLocalSharedTransitionScopeDefaultsToNull() {
        var sharedTransitionScope: SharedTransitionScope? = null

        composeTestRule.setContent {
            sharedTransitionScope = LocalSharedTransitionScope.current
        }

        composeTestRule.runOnIdle {
            assertNull(sharedTransitionScope)
        }
    }

    @Test
    fun whenNoProviderSuppliesAValueThenLocalAnimatedVisibilityScopeDefaultsToNull() {
        var animatedVisibilityScope: AnimatedVisibilityScope? = null

        composeTestRule.setContent {
            animatedVisibilityScope = LocalAnimatedVisibilityScope.current
        }

        composeTestRule.runOnIdle {
            assertNull(animatedVisibilityScope)
        }
    }
}
