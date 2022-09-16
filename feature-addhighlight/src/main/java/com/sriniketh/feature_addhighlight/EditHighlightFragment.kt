package com.sriniketh.feature_addhighlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.sriniketh.feature_addhighlight.databinding.EditHighlightFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditHighlightFragment : Fragment() {

    private var _binding: EditHighlightFragmentBinding? = null
    private val binding: EditHighlightFragmentBinding
        get() = checkNotNull(_binding)

    private val viewModel: EditHighlightFragmentViewModel by viewModels()
    private val args: EditHighlightFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EditHighlightFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.highlightEdittext.setText(args.translatedText)
        binding.highlightSaveButton.setOnClickListener {
            viewModel.saveHighlight(args.bookId, args.translatedText)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    when (uiState) {
                        is EditHighlightUIState.Initial -> {
                            binding.saveProgress.hide()
                        }
                        is EditHighlightUIState.Loading -> {
                            binding.saveProgress.show()
                        }
                        is EditHighlightUIState.AddHighlightSuccess -> {
                            binding.saveProgress.hide()
                            binding.highlightSaveButton.isEnabled = false
                        }
                        is EditHighlightUIState.Failure -> {
                            binding.saveProgress.hide()
                            Snackbar.make(binding.root, getString(uiState.errorMessage), Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
