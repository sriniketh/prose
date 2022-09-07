package com.sriniketh.feature_addhighlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.sriniketh.feature_addhighlight.databinding.EditHighlightFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EditHighlightFragment : Fragment() {

    private var _binding: EditHighlightFragmentBinding? = null
    private val binding: EditHighlightFragmentBinding
        get() = checkNotNull(_binding)

    private val args: EditHighlightFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = EditHighlightFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.highlightEdittext.setText(args.translatedText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
