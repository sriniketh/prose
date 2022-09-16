package com.sriniketh.feature_bookinfo

import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.snackbar.Snackbar
import com.squareup.phrase.Phrase
import com.sriniketh.feature_bookinfo.databinding.BookInfoFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookInfoFragment : Fragment() {

    private var _binding: BookInfoFragmentBinding? = null
    private val binding: BookInfoFragmentBinding
        get() = checkNotNull(_binding)

    private val viewModel: BookInfoFragmentViewModel by viewModels()
    private val args: BookInfoFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BookInfoFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {

            viewModel.getBookDetail(args.volumeid)

            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is BookInfoUiState.Initial -> binding.searchProgress.hide()
                        is BookInfoUiState.Loading -> binding.searchProgress.show()
                        is BookInfoUiState.BookInfoLoadSuccess -> {
                            binding.searchProgress.hide()
                            updateBookState(uiState)
                        }
                        is BookInfoUiState.AddToBookshelfSuccess -> {
                            binding.searchProgress.hide()
                            binding.bookDetailAddButton.isEnabled = false
                            binding.bookDetailAddButton.isClickable = false
                        }
                        is BookInfoUiState.Failure -> {
                            binding.searchProgress.hide()
                            Snackbar.make(
                                binding.root,
                                getString(uiState.errorMessage),
                                Snackbar.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateBookState(uiState: BookInfoUiState.BookInfoLoadSuccess) {
        val bookInfo = uiState.book.info
        binding.bookDetailTitle.text = bookInfo.title
        binding.bookDetailAuthors.text = bookInfo.authors.joinToString(",")
        bookInfo.thumbnailLink?.let {
            val uri = Uri.parse(it).buildUpon().apply { scheme("https") }.build()
            binding.bookDetailThumbnail.load(uri) {
                crossfade(true)
            }
        }
        binding.bookDetailAddButton.setOnClickListener {
            uiState.addBookToShelf(uiState.book)
        }
        bookInfo.description?.let {
            binding.bookDetailDescription.text = Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY)
        }
        if (bookInfo.averageRating != null && bookInfo.ratingsCount != null) {
            binding.bookDetailRating.text =
                Phrase.from(getString(R.string.book_info_ratings_template))
                    .put("rating_value", bookInfo.averageRating.toString())
                    .put("rating_count", bookInfo.ratingsCount.toString())
                    .format()
        }
        bookInfo.pageCount?.let {
            binding.bookDetailPageCount.text =
                Phrase.from(getString(R.string.book_info_pagecount_template))
                    .put("page_count", it)
                    .format()
        }
        bookInfo.publisher?.let {
            binding.bookDetailPublisher.text =
                Phrase.from(getString(R.string.book_info_publisher_template))
                    .put("publisher", it)
                    .format()
        }
        bookInfo.publishedDate?.let {
            binding.bookDetailPublishDate.text =
                Phrase.from(getString(R.string.book_info_publish_date_template))
                    .put("date_published", it)
                    .format()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
