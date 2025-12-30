package com.royrao.codelens

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

/** CameraX 图像分析器，将每一帧转换为 ML Kit InputImage 并识别条码。 */
class BarcodeAnalyzer(private val onResult: (String) -> Unit) : ImageAnalysis.Analyzer {

    // 使用离线版 BarcodeScanning
    private val scanner = BarcodeScanning.getClient()

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner
                .process(image)
                .addOnSuccessListener { barcodes ->
                    // 只要识别到一个，就回调并停止（根据需求，这里简单取第一个非空值）
                    barcodes.firstOrNull()?.rawValue?.let { rawValue -> onResult(rawValue) }
                }
                .addOnFailureListener { e -> Log.e("BarcodeAnalyzer", "Scan failed", e) }
                .addOnCompleteListener {
                    // 必须关闭 imageProxy 以继续下一帧
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
