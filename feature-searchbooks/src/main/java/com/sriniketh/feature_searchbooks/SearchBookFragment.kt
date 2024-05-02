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
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchBookFragment : Fragment() {

    private val viewModel: SearchBookFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    AppSurface {
                        val uiState: BookSearchUiState by viewModel.searchUiState.collectAsStateWithLifecycle()
                        SearchBookScreen(
                            uiState = uiState,
                            searchForBooks = { query ->
                                viewModel.searchForBook(query)
                            },
                            navigateToBookInfo = { volumeId ->
                                navigateToBookInfoFragment(volumeId)
                            },
                            resetSearch = {
                                viewModel.resetSearch()
                            }
                        )
                    }
                }
            }
        }
    }

    private fun navigateToBookInfoFragment(volumeId: String) {
        val action = SearchBookFragmentDirections.searchToBookinfo(volumeId)
        findNavController().navigate(action)
    }
}
