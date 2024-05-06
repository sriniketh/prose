package com.sriniketh.feature_addhighlight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.sriniketh.core_platform.permissions.CameraPermissionChecker
import com.sriniketh.feature_addhighlight.databinding.CameraFragmentBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: CameraFragmentBinding? = null
    private val binding: CameraFragmentBinding
        get() = checkNotNull(_binding)

    @Inject
    lateinit var textAnalyzer: TextAnalyzer

    @Inject
    lateinit var cameraPermissionChecker: CameraPermissionChecker

    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val args: CameraFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = CameraFragmentBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.captureButton.setOnClickListener {
            val bookId = args.bookId
            val translatedText = getTranslatedTextFromAnalyzer()
            val action = CameraFragmentDirections.cameraToInputhighlightAction(
                translatedText = translatedText,
                bookId = bookId
            )
            findNavController().navigate(action)
        }

        if (cameraPermissionChecker.hasPermissionBeenGranted(requireContext())) {
            showCameraPreview()
        } else {
            cameraPermissionChecker.requestCameraPermission(this, ::showCameraPreview) {
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getTranslatedTextFromAnalyzer(): String {
        val sb = StringBuilder()
        textAnalyzer.lastSeenText?.textBlocks?.forEach {
            sb.appendLine(it.text)
            sb.appendLine()
        }
        return sb.toString()
    }

    private fun showCameraPreview() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
                val imageAnalyzer = ImageAnalysis.Builder().build().also {
                    it.setAnalyzer(cameraExecutor, textAnalyzer)
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }
}
