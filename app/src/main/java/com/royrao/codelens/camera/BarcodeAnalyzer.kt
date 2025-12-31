package com.royrao.codelens.camera

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/** Analyzes frames from CameraX using MLKit Barcode Scanning. */
class BarcodeAnalyzer(private val onResult: (List<Barcode>, Int, Int, Int) -> Unit) :
    ImageAnalysis.Analyzer {

    // Get local bundled scanner
    private val scanner = BarcodeScanning.getClient()

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)

            // Pass the dimensions of the *input image buffer* (before rotation is applied logic,
            // but MLKit handles rotation). However, for coordinate mapping, we need to know
            // the dimensions of the image that the BoundingBoxes are relative to.
            // MLKit BoundingBoxes are relative to the *unrotated* image if using fromMediaImage?
            // Actually, MLKit documentation says:
            // "The bounding box is relative to the image."
            // We'll pass the proxy dimensions and rotation to allow the UI to calculate.
            val width = imageProxy.width
            val height = imageProxy.height

            scanner
                .process(image)
                .addOnSuccessListener { barcodes ->
                    // Always report results, even if empty (to clear overlay)
                    onResult(barcodes, width, height, rotationDegrees)
                }
                .addOnFailureListener {
                    // Log failure?
                    onResult(emptyList(), width, height, rotationDegrees)
                }
                .addOnCompleteListener { imageProxy.close() }
        } else {
            imageProxy.close()
        }
    }
}
