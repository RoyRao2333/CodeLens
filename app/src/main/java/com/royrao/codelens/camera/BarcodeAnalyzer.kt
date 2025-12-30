package com.royrao.codelens.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/**
 * Analyzes frames from CameraX using MLKit Barcode Scanning.
 */
class BarcodeAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {
    
    // Get local bundled scanner
    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let {
                            onResult(it)
                            // Stop after first successful result if desired, 
                            // but usually we rely on UI to unbind or ignore subsequent.
                            // Here we just callback.
                        }
                    }
                }
                .addOnFailureListener {
                    // Log failure?
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
             imageProxy.close()
        }
    }
}
