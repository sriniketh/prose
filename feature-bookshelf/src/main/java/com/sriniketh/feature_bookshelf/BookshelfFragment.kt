package com.sriniketh.feature_bookshelf

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
import com.sriniketh.core_design.R
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLEncoder

@AndroidEntryPoint
class BookshelfFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AppTheme {
                    AppSurface {
                        BookshelfScreen(
                            goToSearch = {
                                val navOptions = NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_from_bottom)
                                    .setExitAnim(R.anim.slide_out_top)
                                    .setPopEnterAnim(R.anim.slide_from_top)
                                    .setPopExitAnim(R.anim.slide_out_bottom)
                                    .build()
                                val request = NavDeepLinkRequest.Builder
                                    .fromUri("android-app://com.sriniketh.prose/to_searchbooks_fragment".toUri())
                                    .build()
                                findNavController().navigate(request, navOptions)
                            },
                            goToHighlight = { bookId ->
                                val navOptions = NavOptions.Builder()
                                    .setEnterAnim(R.anim.slide_from_right)
                                    .setExitAnim(R.anim.slide_to_left)
                                    .setPopEnterAnim(R.anim.slide_from_left)
                                    .setPopExitAnim(R.anim.slide_to_right)
                                    .build()
                                val encodedBookId = URLEncoder.encode(bookId, "UTF-8")
                                val request = NavDeepLinkRequest.Builder
                                    .fromUri("android-app://com.sriniketh.prose/to_highlights_fragment/$encodedBookId".toUri())
                                    .build()
                                findNavController().navigate(request, navOptions)
                            }
                        )
                    }
                }
            }
        }
    }
}
