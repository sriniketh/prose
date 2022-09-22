package com.sriniketh.feature_searchbooks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.sriniketh.feature_searchbooks.databinding.SearchBookFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchBookFragment : Fragment() {

    private var _binding: SearchBookFragmentBinding? = null
    private val binding: SearchBookFragmentBinding
        get() = checkNotNull(_binding)

    private var _searchAdapter: SearchBookRecyclerViewAdapter? = null
    private val searchAdapter: SearchBookRecyclerViewAdapter
        get() = checkNotNull(_searchAdapter)

    private val viewModel: SearchBookFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SearchBookFragmentBinding.inflate(inflater)
        _searchAdapter = SearchBookRecyclerViewAdapter()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
            addItemDecoration(
                DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL)
            )
        }

        with(binding.searchEditText) {
            doAfterTextChanged { editable ->
                val text = editable.toString()
                if (text.length > 3) {
                    viewModel.searchForBook(text)
                }
            }
            setOnEditorActionListener { textView, actionId, _ ->
                return@setOnEditorActionListener when (actionId) {
                    EditorInfo.IME_ACTION_SEARCH -> {
                        viewModel.searchForBook(textView.text.toString())
                        true
                    }
                    else -> false
                }
            }
        }
        showSoftKeyboard(binding.searchEditText)

        viewModel.goToBookInfo = { volumeId ->
            navigateToBookInfoFragment(volumeId)
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.searchUiState.collect { searchUiState ->
                    when (searchUiState) {
                        is BookSearchUiState.Initial -> binding.searchProgress.hide()
                        is BookSearchUiState.Loading -> binding.searchProgress.show()
                        is BookSearchUiState.Success -> {
                            binding.searchProgress.hide()
                            binding.searchEditTextLayout.error = null
                            searchAdapter.submitList(searchUiState.bookUiStates)
                        }
                        is BookSearchUiState.Failure -> {
                            binding.searchProgress.hide()
                            binding.searchEditTextLayout.error = getString(searchUiState.errorMessage)
                        }
                    }
                }
            }
        }
    }

    private fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun navigateToBookInfoFragment(volumeId: String) {
        val action = SearchBookFragmentDirections.searchToBookinfo(volumeId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _searchAdapter = null
    }
}
