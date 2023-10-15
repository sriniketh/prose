package com.sriniketh.feature_addhighlight

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.sriniketh.core_platform.permissions.CameraPermissionChecker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CameraFragment : Fragment() {

    @Inject
    lateinit var textAnalyzer: TextAnalyzer

    @Inject
    lateinit var cameraPermissionChecker: CameraPermissionChecker

//    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
//    private val args: CameraFragmentArgs by navArgs()

    private val wasCameraPermissionGranted: MutableState<Boolean> = mutableStateOf(false)
    private val cameraPermissionResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                findNavController().navigateUp()
            } else {
                wasCameraPermissionGranted.value = true
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wasCameraPermissionGranted.value =
            cameraPermissionChecker.hasPermissionBeenGranted(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    CameraScreen(wasCameraPermissionGranted)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun CameraScreen(isCameraPermissionAvailable: State<Boolean>) {
        if (!isCameraPermissionAvailable.value) {
            cameraPermissionResultLauncher.launch(android.Manifest.permission.CAMERA)
        } else {
            val lifecycleCameraController = remember {
                LifecycleCameraController(requireContext()).apply {
                    setEnabledUseCases(CameraController.IMAGE_CAPTURE)
                }
            }

            Scaffold { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    CameraPreview(
                        controller = lifecycleCameraController, modifier = Modifier.fillMaxSize()
                    )

                    IconButton(modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(24.dp), onClick = {}) {
                        val shutterDrawable = AppCompatResources.getDrawable(
                            LocalContext.current, R.drawable.shape_shutter
                        )
                        Image(
                            painter = rememberDrawablePainter(drawable = shutterDrawable),
                            contentDescription = stringResource(id = R.string.capture_button_text),
                            modifier = Modifier
                                .height(72.dp)
                                .width(72.dp)
                        )
                    }
                }
            }
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding.captureButton.setOnClickListener {
//            val bookId = args.bookId
//            val translatedText = getTranslatedTextFromAnalyzer()
//            val action = CameraFragmentDirections.cameraToEdithighlightAction(
//                translatedText = translatedText, bookId = bookId
//            )
//            findNavController().navigate(action)
//        }
//
//        if (cameraPermissionChecker.hasPermissionBeenGranted(requireContext())) {
//            CameraPreview()
//        } else {
//            cameraPermissionChecker.requestCameraPermission(this, ::CameraPreview) {
//                findNavController().navigateUp()
//            }
//        }
//    }
//
//    private fun getTranslatedTextFromAnalyzer(): String {
//        val sb = StringBuilder()
//        textAnalyzer.lastSeenText?.textBlocks?.forEach {
//            sb.appendLine(it.text)
//            sb.appendLine()
//        }
//        return sb.toString()
//    }
//
//    private fun CameraPreview() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
//        cameraProviderFuture.addListener(
//            {
//                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//                val preview = Preview.Builder().build().also {
//                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
//                }
//                val imageAnalyzer = ImageAnalysis.Builder().build().also {
//                    it.setAnalyzer(cameraExecutor, textAnalyzer)
//                }
//
//                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//                try {
//                    cameraProvider.unbindAll()
//                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
//                } catch (exception: Exception) {
//                    Timber.e(exception)
//                }
//            }, ContextCompat.getMainExecutor(requireContext())
//        )
//    }
}
