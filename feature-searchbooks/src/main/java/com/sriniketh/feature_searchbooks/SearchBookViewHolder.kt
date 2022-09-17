package com.sriniketh.feature_searchbooks

import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.sriniketh.feature_searchbooks.databinding.SearchBookResultCardBinding

class SearchBookViewHolder(
    private val binding: SearchBookResultCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uiState: BookUiState) {
        binding.searchResultBookTitle.text = uiState.title
        uiState.subtitle?.let { binding.searchResultBookSubtitle.text = it }
        binding.searchResultBookAuthors.text = uiState.authors.joinToString(", ")
        uiState.thumbnailLink?.let { url ->
            val uri = Uri.parse(url).buildUpon().apply { scheme("https") }.build()
            binding.searchResultImage.load(uri) {
                crossfade(true)
            }
        }
        binding.root.setOnClickListener {
            uiState.viewDetail(uiState.id)
        }
    }
}
