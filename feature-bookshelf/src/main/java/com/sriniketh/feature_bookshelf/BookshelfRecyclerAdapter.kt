package com.sriniketh.feature_bookshelf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sriniketh.core_models.book.Book
import com.sriniketh.feature_bookshelf.databinding.BookshelfItemCardBinding

class BookshelfRecyclerAdapter : ListAdapter<BookUIState, BookshelfViewHolder>(BookshelfDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookshelfViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = BookshelfItemCardBinding.inflate(layoutInflater, parent, false)
        return BookshelfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookshelfViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object BookshelfDiffUtil : DiffUtil.ItemCallback<BookUIState>() {
        override fun areItemsTheSame(oldItem: BookUIState, newItem: BookUIState): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookUIState, newItem: BookUIState): Boolean {
            return oldItem == newItem
        }
    }
}
