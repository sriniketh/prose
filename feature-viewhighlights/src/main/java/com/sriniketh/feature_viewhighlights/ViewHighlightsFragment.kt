package com.sriniketh.feature_viewhighlights

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sriniketh.feature_viewhighlights.databinding.ViewHighlightsFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ViewHighlightsFragment : Fragment() {

    private var _binding: ViewHighlightsFragmentBinding? = null
    private val binding: ViewHighlightsFragmentBinding
        get() = checkNotNull(_binding)

    private var _highlightsAdapter: HighlightsRecyclerAdapter? = null
    private val highlightsAdapter: HighlightsRecyclerAdapter
        get() = checkNotNull(_highlightsAdapter)

    private val viewModel: ViewHighlightsFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ViewHighlightsFragmentBinding.inflate(inflater)
        _highlightsAdapter = HighlightsRecyclerAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.highlightsRecyclerview.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = highlightsAdapter
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        arguments?.getString("bookId")?.let {
            viewModel.getHighlights(it)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.highlightsUIStateFlow.collect { uiState ->
                    when (uiState) {
                        is ViewHighlightsUIState.Initial -> binding.fetchProgress.hide()
                        is ViewHighlightsUIState.Loading -> binding.fetchProgress.show()
                        is ViewHighlightsUIState.Success -> {
                            binding.fetchProgress.hide()
                            binding.highlightsRecyclerview.visibility = View.VISIBLE
                            binding.noHighlightsText.visibility = View.GONE
                            highlightsAdapter.submitList(uiState.highlightsUIState)
                        }
                        is ViewHighlightsUIState.SuccessNoHighlights -> {
                            binding.fetchProgress.hide()
                            binding.highlightsRecyclerview.visibility = View.GONE
                            binding.noHighlightsText.visibility = View.VISIBLE
                        }
                        is ViewHighlightsUIState.Failure -> {
                            binding.fetchProgress.hide()
                            Snackbar.make(
                                binding.root, getString(uiState.errorMessage), Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _highlightsAdapter = null
    }
}
