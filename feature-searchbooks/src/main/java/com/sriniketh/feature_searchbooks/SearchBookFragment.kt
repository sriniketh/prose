package com.sriniketh.feature_searchbooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchBookFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    AppSurface {
                        SearchBookScreen(
                            goToBookInfo = { navigateToBookInfoFragment(it) }
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
