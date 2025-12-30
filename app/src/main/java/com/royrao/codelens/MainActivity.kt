package com.royrao.codelens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.royrao.codelens.camera.CameraXManager
import com.royrao.codelens.ui.ScanResultDialog
import com.royrao.codelens.utils.DeviceCapabilityManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ensure capabilities are checked
        DeviceCapabilityManager.checkAndSaveCapabilities(this)
        
        val isGmsAvailable = DeviceCapabilityManager.getCachedGmsState(this)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(isGmsAvailable = isGmsAvailable)
                }
            }
        }
    }

    @Composable
    fun MainScreen(isGmsAvailable: Boolean) {
        val context = LocalContext.current
        var scanResult by remember { mutableStateOf<String?>(null) }
        var showResultDialog by remember { mutableStateOf(false) }

        // Callback for handling results
        val onResult: (String) -> Unit = { result ->
             handleScanResult(result) {
                 scanResult = it
                 showResultDialog = true
             }
        }

        if (showResultDialog && scanResult != null) {
            ScanResultDialog(
                text = scanResult!!,
                onDismiss = {
                    showResultDialog = false
                    scanResult = null
                    // If GMS, we might want to restart scanner or just close?
                    // For now, let's close activity if it was a quick action, or reset.
                    // But requirement says: "If link -> Intent -> finish".
                    // If text -> Dialog.
                    // We stick to simple logic: Dialog dismiss just stays in app or re-enables scanner?
                    // For simplicity: Dismissing dialog re-runs scanner if Offline, or closes if GMS? 
                    // Actually, let's just keep the dialog open until user acts.
                },
                onCopy = {
                     val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                     val clip = android.content.ClipData.newPlainText("Scan Result", scanResult)
                     clipboard.setPrimaryClip(clip)
                     Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            if (isGmsAvailable) {
                GmsScanner(onResult)
            } else {
                CameraXScanner(onResult)
            }
        }
    }

    private fun handleScanResult(text: String, onText: (String) -> Unit) {
        if (text.startsWith("http://") || text.startsWith("https://") ||
            text.startsWith("magnet:?") || text.startsWith("exp://")) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                startActivity(intent)
                finish()
            } catch(e: Exception) {
                onText(text)
            }
        } else {
            onText(text)
        }
    }

    @Composable
    fun GmsScanner(onResult: (String) -> Unit) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val scanner = GmsBarcodeScanning.getClient(context)
            
            // Optional: Module install check could go here, but GmsBarcodeScanning handles it.
            
            scanner.startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let { onResult(it) } ?: run {
                        // Scan cancelled or empty
                         finish()
                    }
                }
                .addOnCanceledListener {
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "GMS Scan Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
        
        Box(contentAlignment = Alignment.Center) {
             Text("Running GMS Scanner...")
        }
    }

    @Composable
    fun CameraXScanner(onResult: (String) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        var hasPermission by remember { mutableStateOf(false) }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                hasPermission = granted
                if (!granted) {
                     Toast.makeText(context, "Camera permission needed", Toast.LENGTH_SHORT).show()
                }
            }
        )

        LaunchedEffect(Unit) {
            launcher.launch(android.Manifest.permission.CAMERA)
        }

        if (hasPermission) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    CameraXManager.startCamera(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        previewView = previewView,
                        onResult = onResult
                    )
                }
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text("Waiting for permission...")
            }
        }
    }
}
