package com.royrao.codelens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.royrao.codelens.camera.CameraXManager
import com.royrao.codelens.ui.ScanResultDialog
import com.royrao.codelens.ui.components.BubbleOverlay
import com.royrao.codelens.utils.ScanParsedResult
import com.royrao.codelens.utils.ScanResultDispatcher
import kotlin.math.max
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private var shouldLaunchGms by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        handleIntent(intent)

        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(
                        shouldLaunchGms = shouldLaunchGms,
                        onGmsLaunchHandled = { shouldLaunchGms = false },
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (intent.action == com.royrao.codelens.service.QuickScanTileService.ACTION_QUICK_SCAN) {
            shouldLaunchGms = true
        }
    }

    @Composable
    fun MainScreen(shouldLaunchGms: Boolean, onGmsLaunchHandled: () -> Unit) {
        val context = LocalContext.current
        var scanResultTextForDialog by remember { mutableStateOf<String?>(null) }
        var showResultDialog by remember { mutableStateOf(false) }

        // State for scanned bubbles
        val scannedBubbles = remember { mutableStateListOf<ScanParsedResult>() }

        // Callback for handling results (Adding bubbles)
        val onResultHelper: (String) -> Unit = { rawText ->
            // De-duplicate: Ensure no existing bubble has the same content.
            // Using rawValue for comparison is sufficient as ScanResultDispatcher.resolve is
            // deterministic for same input.
            val alreadyExists = scannedBubbles.any { it.rawValue == rawText }
            if (!alreadyExists) {
                val result = ScanResultDispatcher.resolve(context, rawText)
                // Add to start (index 0). With reverseLayout=true in LazyColumn, passing index 0
                // adds to the "bottom" visually.
                scannedBubbles.add(0, result)
            }
        }

        // Always render CameraX as background/default
        // CameraXScanner will call onResult for ANY detected barcode
        CameraXScanner(onResult = onResultHelper)

        // Overlay: Bubble List
        // Fix: Explicitly align Box content to BottomCenter to ensure LazyColumn starts from
        // bottom.
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            BubbleOverlay(
                bubbles = scannedBubbles,
                onBubbleClick = { result ->
                    ScanResultDispatcher.launch(
                        context = context,
                        intent = result.intent ?: Intent(),
                        onFail = {
                            scanResultTextForDialog = it
                            showResultDialog = true
                        },
                        rawValue = result.rawValue,
                    )

                    if (result.intent == null) {
                        scanResultTextForDialog = result.rawValue
                        showResultDialog = true
                    }
                },
            )
        }

        // Handle GMS Launch (Quick Scan)
        if (shouldLaunchGms) {
            GmsScannerLauncher(
                onResult = {
                    onResultHelper(it)
                    onGmsLaunchHandled()
                },
                onDismiss = { onGmsLaunchHandled() },
            )
        }

        if (showResultDialog && scanResultTextForDialog != null) {
            ScanResultDialog(
                text = scanResultTextForDialog!!,
                onDismiss = {
                    showResultDialog = false
                    scanResultTextForDialog = null
                },
                onCopy = {
                    val clipboard =
                        context.getSystemService(Context.CLIPBOARD_SERVICE)
                            as android.content.ClipboardManager
                    val clip =
                        android.content.ClipData.newPlainText(
                            "Scan Result",
                            scanResultTextForDialog,
                        )
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "已复制", Toast.LENGTH_SHORT).show()
                },
            )
        }
    }

    @Composable
    fun GmsScannerLauncher(onResult: (String) -> Unit, onDismiss: () -> Unit) {
        val context = LocalContext.current
        LaunchedEffect(Unit) {
            val scanner = GmsBarcodeScanning.getClient(context)
            scanner
                .startScan()
                .addOnSuccessListener { barcode ->
                    barcode.rawValue?.let { onResult(it) } ?: onDismiss()
                }
                .addOnCanceledListener { onDismiss() }
                .addOnFailureListener {
                    Toast.makeText(context, "GMS Scan Failed: ${it.message}", Toast.LENGTH_SHORT)
                        .show()
                    onDismiss()
                }
        }
    }

    @Composable
    fun CameraXScanner(onResult: (String) -> Unit) {
        val context = LocalContext.current
        val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        var hasPermission by remember { mutableStateOf(false) }

        // State for detected barcodes and image info
        var detectedBarcodes by remember { mutableStateOf<List<Barcode>>(emptyList()) }
        var sourceInfo by remember {
            mutableStateOf<Triple<Int, Int, Int>?>(null)
        } // w, h, rotation

        // Process detections continuously
        LaunchedEffect(detectedBarcodes) {
            detectedBarcodes.forEach { barcode -> barcode.rawValue?.let { onResult(it) } }
        }

        val launcher =
            rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    hasPermission = granted
                    if (!granted) {
                        Toast.makeText(context, "Camera permission needed", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
            )

        LaunchedEffect(Unit) { launcher.launch(android.Manifest.permission.CAMERA) }

        if (hasPermission) {
            Box(modifier = Modifier.fillMaxSize()) {
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).apply {
                            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                            scaleType = PreviewView.ScaleType.FILL_CENTER
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { previewView ->
                        CameraXManager.startCamera(
                            context = context,
                            lifecycleOwner = lifecycleOwner,
                            previewView = previewView,
                            onResult = { barcodes, width, height, rotation ->
                                detectedBarcodes = barcodes
                                sourceInfo = Triple(width, height, rotation)
                            },
                        )
                    },
                )

                // Overlay Layer (Blue Dots)
                if (detectedBarcodes.isNotEmpty() && sourceInfo != null) {
                    val (imgW, imgH, rotation) = sourceInfo!!
                    val density = LocalDensity.current

                    // Calculate mapping variables
                    val (srcWidth, srcHeight) =
                        if (rotation == 90 || rotation == 270) {
                            Pair(imgH, imgW)
                        } else {
                            Pair(imgW, imgH)
                        }

                    Canvas(
                        modifier =
                            Modifier.fillMaxSize()
                                // Keep tap gestures if user wants to tap DOT to trigger bubble
                                // (technically it's already triggered)
                                // Or maybe tap dot to OPEN?
                                // User requirement: "Click any bubble... to execute".
                                // I'll leave tap gesture but make it define the "add bubble" action
                                // just in case logic needs it, or remove it?
                                // Since detecting adds bubble, tapping dot is redundant but
                                // harmless.
                                // I'll keep it as a failsafe or manual trigger.
                                .pointerInput(detectedBarcodes) {
                                    detectTapGestures(
                                        onTap = { tapOffset ->
                                            Log.d("royrao", "Tap at: $tapOffset")
                                            val viewW = size.width.toFloat()
                                            val viewH = size.height.toFloat()
                                            val scale = max(viewW / srcWidth, viewH / srcHeight)
                                            val offsetX = (viewW - srcWidth * scale) / 2
                                            val offsetY = (viewH - srcHeight * scale) / 2

                                            val clickedCode =
                                                detectedBarcodes.firstOrNull { barcode ->
                                                    barcode.boundingBox?.let { rect ->
                                                        val left = rect.left * scale + offsetX
                                                        val top = rect.top * scale + offsetY
                                                        val right = rect.right * scale + offsetX
                                                        val bottom = rect.bottom * scale + offsetY
                                                        val padding = 48 * density.density

                                                        tapOffset.x >= left - padding &&
                                                            tapOffset.x <= right + padding &&
                                                            tapOffset.y >= top - padding &&
                                                            tapOffset.y <= bottom + padding
                                                    } ?: false
                                                }
                                            clickedCode?.rawValue?.let(onResult)
                                        }
                                    )
                                }
                    ) {
                        val viewW = size.width
                        val viewH = size.height
                        val scale = max(viewW / srcWidth, viewH / srcHeight)
                        val offsetX = (viewW - srcWidth * scale) / 2
                        val offsetY = (viewH - srcHeight * scale) / 2

                        detectedBarcodes.forEach { barcode ->
                            barcode.boundingBox?.let { rect ->
                                val left = rect.left * scale + offsetX
                                val top = rect.top * scale + offsetY
                                val right = rect.right * scale + offsetX
                                val bottom = rect.bottom * scale + offsetY

                                val centerX = (left + right) / 2
                                val centerY = (top + bottom) / 2
                                val radius = 10.dp.toPx()

                                drawCircle(
                                    color = Color(0xFF2196F3),
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = radius,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 2.dp.toPx()),
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(contentAlignment = Alignment.Center) { Text("Waiting for permission...") }
        }
    }
}
