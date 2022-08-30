package com.sriniketh.feature_bookshelf

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sriniketh.feature_bookshelf.databinding.BookshelfItemCardBinding

class BookshelfViewHolder(
    private val binding: BookshelfItemCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uiState: BookUIState) {
        binding.bookshelfItemTitle.text = uiState.info.title
        binding.bookshelfItemAuthors.text = uiState.info.authors.joinToString(", ")
        uiState.info.thumbnailLink?.let { url ->
            val uri = Uri.parse(url).buildUpon().apply { scheme("https") }.build()
            binding.bookshelfItemImage.load(uri) {
                crossfade(true)
            }
        }
        binding.root.setOnClickListener {
            uiState.viewBook(uiState.id)
        }
    }
}
