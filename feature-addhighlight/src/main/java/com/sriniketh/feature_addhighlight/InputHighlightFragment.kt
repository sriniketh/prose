package com.sriniketh.feature_addhighlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputHighlightFragment : Fragment() {

    private val viewModel: InputHighlightFragmentViewModel by viewModels()
    private val args: InputHighlightFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AppTheme {
                    AppSurface {
                        val uiState: InputHighlightUiState by viewModel.uiState.collectAsStateWithLifecycle()

                        viewModel.setHighlightText(args.translatedText)

                        InputHighlightScreen(
                            uiState = uiState,
                            saveHighlight = { text ->
                                viewModel.saveHighlight(args.bookId, text)
                            },
                            goBack = { findNavController().navigateUp() }
                        )
                    }
                }
            }
        }
    }
}
