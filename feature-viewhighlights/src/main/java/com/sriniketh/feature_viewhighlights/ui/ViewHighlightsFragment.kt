package com.sriniketh.feature_viewhighlights.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.feature_viewhighlights.ViewHighlightsFragmentViewModel
import com.sriniketh.feature_viewhighlights.ViewHighlightsUIState
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
            viewModel.loadHighlights(args.bookId)
            setContent {
                MaterialTheme {
                    val uiState: ViewHighlightsUIState by viewModel.highlightsUIStateFlow.collectAsStateWithLifecycle()
                    ViewHighlightsScreen(
                        uiState = uiState,
                        goBack = { findNavController().navigateUp() },
                        addHighlight = {}
                    )
                }
            }
        }
    }
}
