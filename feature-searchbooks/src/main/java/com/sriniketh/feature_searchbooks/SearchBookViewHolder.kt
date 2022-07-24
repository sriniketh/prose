package com.sriniketh.feature_searchbooks

import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sriniketh.core_models.search.Book
import com.sriniketh.feature_searchbooks.databinding.SearchBookResultCardBinding

class SearchBookViewHolder(
    private val binding: SearchBookResultCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(book: Book) {
        binding.searchResultBookTitle.text = book.info.title
        book.info.subtitle?.let { binding.searchResultBookSubtitle.text = it }
        binding.searchResultBookAuthors.text = book.info.authors.joinToString(", ")
        book.info.thumbnailLink?.let { url ->
            binding.searchResultImage.load(url) {
                crossfade(true)
            }
        }
    }
}
