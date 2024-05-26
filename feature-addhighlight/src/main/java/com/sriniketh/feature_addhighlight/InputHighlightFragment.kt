package com.sriniketh.feature_addhighlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.core_design.ui.components.AppSurface
import com.sriniketh.core_design.ui.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InputHighlightFragment : Fragment() {

    private val viewModel: InputHighlightFragmentViewModel by viewModels()
    private val args: InputHighlightFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                AppTheme {
                    AppSurface {
                        val bookId = args.bookId
                        val inputHighlightScreenState: InputHighlightScreenState by viewModel.screenState.collectAsStateWithLifecycle()
                        val editHighlightUiState: EditAndSaveHighlightUiState by viewModel.editHighlightUiState.collectAsStateWithLifecycle()
                        val cameraLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.TakePicture(),
                            onResult = { success ->
                                if (success) {
                                    viewModel.onImageCaptured()
                                } else {
                                    findNavController().navigateUp()
                                }
                            }
                        )

                        when (val screenState = inputHighlightScreenState) {
                            is InputHighlightScreenState.CaptureImage -> {
                                LaunchedEffect(Unit) {
                                    cameraLauncher.launch(screenState.imageUri)
                                }
                            }

                            is InputHighlightScreenState.CropImage -> {
                                CropImageScreen(
                                    imageUri = screenState.imageUri,
                                    onImageCropped = { viewModel.onImageCropped() }
                                )
                            }

                            InputHighlightScreenState.EditAndSaveHighlight -> {
                                EditAndSaveHighlightScreen(
                                    uiState = editHighlightUiState,
                                    updateHighlightText = { highlightText ->
                                        viewModel.onHighlightTextUpdated(highlightText)
                                    },
                                    saveHighlight = { highlight ->
                                        viewModel.onHighlightSaved(bookId, highlight)
                                    },
                                    goBack = {
                                        findNavController().navigateUp()
                                    }
                                )
                            }

                            InputHighlightScreenState.SaveHighlightSuccessful -> {
                                findNavController().navigateUp()
                            }
                        }
                    }
                }
            }
        }
    }
}
