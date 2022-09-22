package com.sriniketh.feature_viewhighlights

import androidx.recyclerview.widget.RecyclerView
import com.squareup.phrase.Phrase
import com.sriniketh.feature_viewhighlights.databinding.HighlightItemCardBinding

class HighlightViewHolder(
    private val binding: HighlightItemCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uiState: HighlightUIState) {
        val context = binding.root.context
        binding.highlightText.text = uiState.text
        binding.highlightSubtext.text = Phrase.from(context.getString(R.string.saved_on_template)).put("datetime", uiState.savedOn).format()
    }
}
