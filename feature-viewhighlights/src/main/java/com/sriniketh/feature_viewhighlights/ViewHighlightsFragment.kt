package com.sriniketh.feature_viewhighlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.core_design.R
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewHighlightsFragment : Fragment() {

    private val args: ViewHighlightsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    AppSurface {
                        ViewHighlightsScreen(
                            bookId = args.bookId,
                            goBack = {
                                findNavController().navigateUp()
                            },
                            goToInputHighlightScreen = {
                                launchInputHighlightScreen(args.bookId)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun launchInputHighlightScreen(bookId: String) {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_from_bottom)
            .setExitAnim(R.anim.slide_out_top)
            .setPopEnterAnim(R.anim.slide_from_top)
            .setPopExitAnim(R.anim.slide_out_bottom)
            .build()
        val request = NavDeepLinkRequest.Builder
            .fromUri("android-app://com.sriniketh.prose/to_input_highlight_fragment?bookId=$bookId".toUri())
            .build()
        findNavController().navigate(request, navOptions)
    }
}
