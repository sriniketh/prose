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
import com.sriniketh.core_design.R
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
                            onEvent = { event ->
                                when (event) {
                                    is ViewHighlightsEvent.OnBackPressed -> findNavController().navigateUp()
                                    is ViewHighlightsEvent.OnCameraPermissionGranted -> {
                                        launchInputHighlightScreen(args.bookId)
                                    }

                                    else -> viewModel.processEvent(event)
                                }
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
