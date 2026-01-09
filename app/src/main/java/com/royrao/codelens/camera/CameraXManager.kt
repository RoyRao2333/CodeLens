package com.royrao.codelens.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.util.concurrent.Executors

object CameraXManager {
  fun startCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onResult: (List<com.google.mlkit.vision.barcode.common.Barcode>, Int, Int, Int) -> Unit,
  ) {
    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

    cameraProviderFuture.addListener(
      {
        val cameraProvider = cameraProviderFuture.get()

        // Resolution Selector (replaces deprecated setTargetAspectRatio)
        val resolutionSelector =
          ResolutionSelector.Builder()
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY)
            .build()

        // Preview
        val preview =
          Preview.Builder().setResolutionSelector(resolutionSelector).build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
          }

        // Image Analysis
        val imageAnalyzer =
          ImageAnalysis.Builder()
            .setResolutionSelector(resolutionSelector)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(Executors.newSingleThreadExecutor(), BarcodeAnalyzer(onResult)) }

        // Select back camera
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
          // Unbind use cases before rebinding
          cameraProvider.unbindAll()

          // Bind use cases to camera
          cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalyzer)
        } catch (exc: Exception) {
          // Log.e(TAG, "Use case binding failed", exc)
        }
      },
      ContextCompat.getMainExecutor(context),
    )
  }
}
