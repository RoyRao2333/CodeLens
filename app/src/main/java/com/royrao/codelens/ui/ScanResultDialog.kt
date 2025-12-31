package com.royrao.codelens.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ScanResultDialog(text: String, onDismiss: () -> Unit, onCopy: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "扫描结果") },
        text = { Text(text = text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onCopy()
                    onDismiss()
                }
            ) {
                Text("复制")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("关闭") } },
    )
}
