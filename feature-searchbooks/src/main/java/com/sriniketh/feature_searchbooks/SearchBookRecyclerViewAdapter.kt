package com.sriniketh.feature_searchbooks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sriniketh.core_models.search.Book
import com.sriniketh.feature_searchbooks.databinding.SearchBookResultCardBinding

class SearchBookRecyclerViewAdapter :
    ListAdapter<Book, SearchBookViewHolder>(SearchBookDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchBookViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = SearchBookResultCardBinding.inflate(layoutInflater, parent, false)
        return SearchBookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchBookViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object SearchBookDiffCallback : DiffUtil.ItemCallback<Book>() {
        override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
            return oldItem == newItem
        }
    }
}
