package com.royrao.codelens.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.URLUtil
import android.widget.Toast
import java.util.regex.Pattern

/**
 * Intelligent dispatcher for scan results. Distinguishes between URLs/Schemes and plain text, and
 * handles the launch intent logic.
 */
object ScanResultDispatcher {

    /**
     * Dispatches the scan result.
     *
     * @param context Context used to start activity.
     * @param rawValue The scanned raw string.
     * @param onShowDialog Callback to show the result dialog (fallback or plain text).
     */
    fun dispatch(context: Context, rawValue: String, onShowDialog: (String) -> Unit) {
        if (isUrlOrScheme(rawValue)) {
            dispatchUrl(context, rawValue, onShowDialog)
        } else {
            // Pure text
            onShowDialog(rawValue)
        }
    }

    private fun isUrlOrScheme(text: String): Boolean {
        // Standard URL check
        if (URLUtil.isValidUrl(text)) return true

        // Custom scheme check (e.g. exp://, magnet:?, weixin://)
        // Scheme must start with a letter, followed by letters, digits, plus, period, or hyphen.
        // We allow standard :// structure OR specific non-slash schemes like magnet:
        val schemePattern =
            Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*://.+|magnet:\\?.+|mailto:.+|tel:.+|geo:.+)")
        return schemePattern.matcher(text).matches()
    }

    private fun dispatchUrl(context: Context, url: String, onShowDialog: (String) -> Unit) {
        try {
            val uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // "Smart Dispatcher" logic: Add NEW_TASK flag as requested
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // Anti-Hijack / OPPO/Xiaomi fix for exp://
            // Hint system that this is a browsable link to avoid some browser interceptions,
            // or correct handling for deep links.
            if (url.startsWith("exp://")) {
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                // Stronger fix: Directly target Expo Go if it's an exp:// link.
                // This prevents browsers from hijacking the intent.
                // If Expo Go is not installed, startActivity will throw ActivityNotFoundException,
                // which is caught below and handled gracefully (showing text).
                intent.setPackage("host.exp.exponent")
            }

            // Start Activity
            context.startActivity(intent)

            // If success, we want to close scanning page?
            // The context might be an Activity.
            if (context is Activity) {
                // We finish the scanning activity so user doesn't come back to camera immediately
                // when back
                // verified
                // But if we want to keep app alive, maybe not?
                // User said: "If successful jump, close scan page"
                context.finish()
            }
        } catch (e: ActivityNotFoundException) {
            // App not installed or no handler
            // Downgrade to text handling
            // Maybe show a specific message? Or just the text?
            // User said: "Downgrade processing, popup alert and show original text"
            // For now, we pass original text to the dialog which has Copy functionality.
            // We can also toast.
            Toast.makeText(context, "未找到可打开该链接的应用", Toast.LENGTH_SHORT).show()
            onShowDialog(url)
        } catch (e: Exception) {
            // Other errors
            onShowDialog(url)
        }
    }
}
