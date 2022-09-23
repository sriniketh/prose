package com.sriniketh.feature_bookshelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sriniketh.core_design.R
import com.sriniketh.feature_bookshelf.databinding.BookshelfFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.net.URLEncoder

@AndroidEntryPoint
class BookshelfFragment : Fragment() {

    private var _binding: BookshelfFragmentBinding? = null
    private val binding: BookshelfFragmentBinding
        get() = checkNotNull(_binding)

    private var _bookshelfAdapter: BookshelfRecyclerAdapter? = null
    private val bookshelfAdapter: BookshelfRecyclerAdapter
        get() = checkNotNull(_bookshelfAdapter)

    private val viewModel: BookshelfFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BookshelfFragmentBinding.inflate(inflater)
        _bookshelfAdapter = BookshelfRecyclerAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bookshelfRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = bookshelfAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        viewModel.getSavedBooks()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.bookshelfUIState.collect { uiState ->
                    when (uiState) {
                        is BookshelfUIState.Initial -> binding.fetchbooksProgress.hide()
                        is BookshelfUIState.Loading -> binding.fetchbooksProgress.show()
                        is BookshelfUIState.Success -> {
                            binding.fetchbooksProgress.hide()
                            binding.bookshelfRecyclerview.visibility = View.VISIBLE
                            binding.bookshelfNoBooksText.visibility = View.GONE
                            bookshelfAdapter.submitList(uiState.bookUIStates)
                        }
                        is BookshelfUIState.SuccessNoBooks -> {
                            binding.fetchbooksProgress.hide()
                            binding.bookshelfRecyclerview.visibility = View.GONE
                            binding.bookshelfNoBooksText.visibility = View.VISIBLE
                        }
                        is BookshelfUIState.Failure -> {
                            binding.fetchbooksProgress.hide()
                            Snackbar.make(
                                binding.root, getString(uiState.errorMessage), Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        viewModel.viewHighlightsForBook = { bookId ->
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

        binding.searchFab.setOnClickListener {
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
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _bookshelfAdapter = null
    }
}
