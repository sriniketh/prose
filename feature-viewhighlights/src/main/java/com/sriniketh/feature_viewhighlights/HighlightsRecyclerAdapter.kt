package com.sriniketh.feature_viewhighlights

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.sriniketh.feature_viewhighlights.databinding.HighlightItemCardBinding

class HighlightsRecyclerAdapter :
    ListAdapter<HighlightUIState, HighlightViewHolder>(HighlightDiffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = HighlightItemCardBinding.inflate(layoutInflater, parent, false)
        return HighlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HighlightViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object HighlightDiffUtil : DiffUtil.ItemCallback<HighlightUIState>() {
        override fun areItemsTheSame(
            oldItem: HighlightUIState,
            newItem: HighlightUIState
        ): Boolean {
            return false
        }

        override fun areContentsTheSame(
            oldItem: HighlightUIState,
            newItem: HighlightUIState
        ): Boolean {
            return false
        }
    }
}
