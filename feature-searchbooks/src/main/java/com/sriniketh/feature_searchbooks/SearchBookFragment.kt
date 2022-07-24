package com.sriniketh.feature_searchbooks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.sriniketh.feature_searchbooks.databinding.SearchBookFragmentBinding
import com.sriniketh.prose.core_network.BooksRemoteDataSource
import com.sriniketh.prose.core_network.model.asBookSearchResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SearchBookFragment : Fragment() {

    private var _binding: SearchBookFragmentBinding? = null
    private val binding: SearchBookFragmentBinding
        get() = checkNotNull(_binding)

    private var _searchAdapter: SearchBookRecyclerViewAdapter? = null
    private val searchAdapter: SearchBookRecyclerViewAdapter
        get() = checkNotNull(_searchAdapter)

    @Inject
    lateinit var remoteDataSource: BooksRemoteDataSource

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
        }
        binding.searchEditButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // TODO move to viewmodel
                val volumes = remoteDataSource.getVolumes(binding.searchEditText.text.toString())
                searchAdapter.submitList(volumes.asBookSearchResult().items)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _searchAdapter = null
    }
}
