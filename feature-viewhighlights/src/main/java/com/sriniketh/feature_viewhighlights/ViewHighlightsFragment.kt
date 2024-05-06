package com.sriniketh.feature_viewhighlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ViewHighlightsFragment : Fragment() {

    private val viewModel: ViewHighlightsFragmentViewModel by viewModels()
    private val args: ViewHighlightsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            viewModel.getHighlights(args.bookId)

            setContent {
                AppTheme {
                    AppSurface {
                        val uiState: ViewHighlightsUIState by viewModel.highlightsUIStateFlow.collectAsStateWithLifecycle()
                        ViewHighlightsScreen(
                            uiState = uiState,
                            addHighlight = {
                                val navOptions = NavOptions.Builder()
                                    .setEnterAnim(com.sriniketh.core_design.R.anim.slide_from_bottom)
                                    .setExitAnim(com.sriniketh.core_design.R.anim.slide_out_top)
                                    .setPopEnterAnim(com.sriniketh.core_design.R.anim.slide_from_top)
                                    .setPopExitAnim(com.sriniketh.core_design.R.anim.slide_out_bottom)
                                    .build()
                                val bookId = args.bookId
                                val request = NavDeepLinkRequest.Builder
                                    .fromUri("android-app://com.sriniketh.prose/to_camera_fragment?bookId=$bookId".toUri())
                                    .build()
                                findNavController().navigate(request, navOptions)
                            },
                            goBack = { findNavController().navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
