package com.sriniketh.feature_bookshelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.sriniketh.feature_bookshelf.databinding.BookshelfFragmentBinding

class BookshelfFragment : Fragment() {

    private var _binding: BookshelfFragmentBinding? = null
    private val binding: BookshelfFragmentBinding
        get() = checkNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BookshelfFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.searchFab.setOnClickListener {
            val request = NavDeepLinkRequest.Builder
                .fromUri("android-app://com.sriniketh.prose/to_searchbooks_fragment".toUri())
                .build()
            findNavController().navigate(request)
        }
    }
}
