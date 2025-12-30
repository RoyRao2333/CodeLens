package com.royrao.codelens

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Patterns
import android.widget.Toast

/**
 * 处理扫描结果：URL跳转或文本展示。
 */
object ResultHandler {
    fun handleResult(context: Context, text: String) {
        if (text.isBlank()) return

        // 简单判断是否为 URL (包括 HTTP, HTTPS, MAGNET, EXP 等协议)
        val isUrl = Patterns.WEB_URL.matcher(text).matches() || 
                    text.startsWith("magnet:?", ignoreCase = true) ||
                    text.startsWith("exp://", ignoreCase = true)

        if (isUrl) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(text))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                
                // 如果是 Activity，跳转后关闭自身
                if (context is Activity) {
                    context.finish()
                }
            } catch (e: Exception) {
                // 如果无法打开（如下载磁力链但无对应 App），回退到复制弹窗
                showTextDialog(context, text)
            }
        } else {
            showTextDialog(context, text)
        }
    }

    private fun showTextDialog(context: Context, text: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.scanned_result)
        builder.setMessage(text)
        builder.setPositiveButton(R.string.copy) { dialog, _ ->
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("scanned_text", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            if (context is Activity) {
                context.finish()
            }
        }
        builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
            // 扫描取消或关闭对话框后，是否重启扫描？这里简单处理为关闭 App (一次性工具属性)
            if (context is Activity) {
                context.finish()
            }
        }
        builder.setOnDismissListener { 
            if (context is Activity) {
                // 防止 Dialog 消失后 Activity 仍停留
                // context.finish() 
                // 上面 NegativeButton 已处理，这里留空防止重复 finish
            }
        }
        builder.show()
    }
}
