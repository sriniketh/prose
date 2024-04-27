package com.sriniketh.feature_searchbooks

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
class BookInfoFragment : Fragment() {

    private val viewModel: BookInfoFragmentViewModel by viewModels()
    private val args: BookInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            viewModel.getBookDetail(args.volumeid)

            setContent {
                AppTheme {
                    AppSurface {
                        val uiState: BookInfoUiState by viewModel.uiState.collectAsStateWithLifecycle()
                        BookInfoScreen(uiState = uiState) {
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }
}
