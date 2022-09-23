package com.sriniketh.feature_viewhighlights

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
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sriniketh.core_design.R
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
    private val args: ViewHighlightsFragmentArgs by navArgs()

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

        binding.viewhighlightsAppbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.getHighlights(args.bookId)

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
                            Snackbar.make(binding.root, getString(uiState.errorMessage), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.addHighlightFab.setOnClickListener {
            val navOptions = NavOptions.Builder()
                .setEnterAnim(R.anim.slide_from_bottom)
                .setExitAnim(R.anim.slide_out_top)
                .setPopEnterAnim(R.anim.slide_from_top)
                .setPopExitAnim(R.anim.slide_out_bottom)
                .build()
            val bookId = args.bookId
            val request = NavDeepLinkRequest.Builder
                .fromUri("android-app://com.sriniketh.prose/to_camera_fragment?bookId=$bookId".toUri())
                .build()
            findNavController().navigate(request, navOptions)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _highlightsAdapter = null
    }
}
