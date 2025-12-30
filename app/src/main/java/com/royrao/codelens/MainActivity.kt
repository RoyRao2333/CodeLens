package com.royrao.codelens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.royrao.codelens.ui.theme.CodeLensTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 检测 GMS 状态
        val isGmsAvailable = DeviceCapabilityManager.isGmsAvailable(this)

        if (isGmsAvailable) {
            // A 分支：GMS 模式
            setContent {
                CodeLensTheme {
                    // 显示简单的 Loading，等待 GMS 扫描器覆盖
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            startGmsScanner()
        } else {
            // B 分支：离线模式
            Toast.makeText(this, R.string.gms_unavailable, Toast.LENGTH_SHORT).show()
            setContent {
                CodeLensTheme {
                    OfflineScanner { result -> ResultHandler.handleResult(this, result) }
                }
            }
        }
    }

    private fun startGmsScanner() {
        // 可选：确保模块已就绪（GMS Code Scanner 是自动下载的）
        val scanner = GmsBarcodeScanning.getClient(this)

        scanner
            .startScan()
            .addOnSuccessListener { barcode ->
                val rawValue = barcode.rawValue
                if (rawValue != null) {
                    ResultHandler.handleResult(this, rawValue)
                } else {
                    finish()
                }
            }
            .addOnCanceledListener {
                // 用户取消扫描
                Log.d("MainActivity", "GMS Scan canceled")
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "GMS Scan error", e)
                // 失败回退到本地扫描或关闭
                Toast.makeText(this, "GMS Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
    }
}
