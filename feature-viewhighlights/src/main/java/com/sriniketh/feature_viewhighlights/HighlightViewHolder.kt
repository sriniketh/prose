package com.sriniketh.feature_viewhighlights

import androidx.recyclerview.widget.RecyclerView
import com.sriniketh.feature_viewhighlights.databinding.HighlightItemCardBinding

class HighlightViewHolder(
    private val binding: HighlightItemCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uiState: HighlightUIState) {
        binding.highlightText.text = uiState.text
        binding.highlightSubtext.text = uiState.savedOn
    }
}
