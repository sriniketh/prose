package com.sriniketh.core_design.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun whenSharedTransitionScopeAndAnimatedVisibilityScopeAreAbsentThenModifierIsReturnedUnchanged() {
        val original = Modifier.testTag("target")
        var result: Modifier? = null

        composeTestRule.setContent {
            result = original.sharedBoundsTransition("key")
        }

        composeTestRule.runOnIdle {
            assertSame(original, result)
        }
    }

    @Test
    fun whenSharedTransitionScopeAndAnimatedVisibilityScopeArePresentThenModifierAppliesSharedBoundsTransition() {
        val original = Modifier.testTag("target")
        var result: Modifier? = null

        composeTestRule.setContent {
            SharedTransitionLayout {
                CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                    AnimatedVisibility(visible = true) {
                        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
                            result = original.sharedBoundsTransition("key")
                        }
                    }
                }
            }
        }

        composeTestRule.runOnIdle {
            assertNotSame(original, result)
        }
    }
}
