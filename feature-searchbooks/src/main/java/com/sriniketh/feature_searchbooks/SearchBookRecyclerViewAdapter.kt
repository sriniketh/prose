package com.sriniketh.feature_searchbooks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sriniketh.feature_searchbooks.databinding.SearchBookResultCardBinding

class SearchBookRecyclerViewAdapter :
    ListAdapter<BookUiState, SearchBookViewHolder>(SearchBookDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SearchBookResultCardBinding.inflate(layoutInflater, parent, false)
        return SearchBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchBookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object SearchBookDiffCallback : DiffUtil.ItemCallback<BookUiState>() {
        override fun areItemsTheSame(oldItem: BookUiState, newItem: BookUiState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookUiState, newItem: BookUiState): Boolean {
            return oldItem == newItem
        }
    }
}
