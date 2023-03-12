package com.sriniketh.feature_viewhighlights

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.phrase.Phrase
import com.sriniketh.feature_viewhighlights.databinding.HighlightItemCardBinding

class HighlightViewHolder(
    private val binding: HighlightItemCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(uiState: HighlightUIState) {
        val context = binding.root.context
        binding.highlightText.text = uiState.text
        binding.highlightSubtext.text = Phrase.from(context.getString(R.string.saved_on_template)).put("datetime", uiState.savedOn).format()
        binding.highlightDeleteIcon.setOnClickListener {
            MaterialAlertDialogBuilder(context)
                .setTitle(context.getString(R.string.delete_dialog_title))
                .setMessage(context.getString(R.string.delete_dialog_message))
                .setPositiveButton(context.getString(R.string.delete_dialog_positive_button_label)) { _, _ ->
                    uiState.onDelete.invoke()
                }
                .setNegativeButton(context.getString(R.string.delete_dialog_negative_button_label)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
}
