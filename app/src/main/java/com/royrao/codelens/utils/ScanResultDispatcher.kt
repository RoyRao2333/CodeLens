package com.royrao.codelens.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.webkit.URLUtil
import android.widget.Toast
import java.util.regex.Pattern

data class ScanParsedResult(
  val rawValue: String,
  val type: ScanType,
  val icon: Drawable?,
  val label: String,
  val intent: Intent?,
)

enum class ScanType {
  URL,
  SCHEME,
  TEXT,
}

/**
 * Intelligent dispatcher for scan results. Distinguishes between URLs/Schemes and plain text, and
 * handles the launch intent logic.
 */
object ScanResultDispatcher {

  /** Resolves the scan result into actionable data (Icon, Label, Intent). */
  fun resolve(context: Context, rawValue: String): ScanParsedResult {
    if (isUrlOrScheme(rawValue)) {
      return resolveUrl(context, rawValue)
    } else {
      // Pure text
      return ScanParsedResult(
        rawValue = rawValue,
        type = ScanType.TEXT,
        icon = null, // UI can provide default text icon
        label = "显示内容", // Or "Copy"
        intent = null, // UI handles text action (Search/Copy)
      )
    }
  }

  /** Legacy dispatch method - retained for compatibility or fallback. */
  fun dispatch(context: Context, rawValue: String, onShowDialog: (String) -> Unit) {
    val result = resolve(context, rawValue)
    if (result.intent != null) {
      launch(context, result.intent, onShowDialog, rawValue)
    } else {
      onShowDialog(rawValue)
    }
  }

  fun launch(context: Context, intent: Intent, onFail: (String) -> Unit, rawValue: String) {
    try {
      context.startActivity(intent)
      if (context is Activity) {
        // heuristic: if we successfully launched an activity, maybe close the scanner?
        // For the bubble UI, we might want to keep it open?
        // Keeping original behavior: finish()
        context.finish()
      }
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(context, "未找到可打开该链接的应用", Toast.LENGTH_SHORT).show()
      onFail(rawValue)
    } catch (e: Exception) {
      onFail(rawValue)
    }
  }

  private fun isUrlOrScheme(text: String): Boolean {
    if (URLUtil.isValidUrl(text)) return true
    val schemePattern =
      Pattern.compile("^([a-zA-Z][a-zA-Z0-9+.-]*://.+|magnet:\\?.+|mailto:.+|tel:.+|geo:.+)")
    return schemePattern.matcher(text).matches()
  }

  private fun resolveUrl(context: Context, url: String): ScanParsedResult {
    try {
      val uri = Uri.parse(url)
      val intent = Intent(Intent.ACTION_VIEW, uri)
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

      // Special handling
      if (url.startsWith("exp://")) {
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        // Remove hardcoded package to allow custom development builds to handle it
        // intent.setPackage("host.exp.exponent")
      }

      val pm = context.packageManager
      val resolveInfo = pm.resolveActivity(intent, 0)

      var icon: Drawable? = null
      var label = "打开"

      if (resolveInfo != null && resolveInfo.activityInfo != null) {
        // Determine if it resolved to the system resolver (chooser) or a specific app
        // If it's a specific app, use its icon and name.
        // Note: On some devices, if multiple apps handle it, it might return the
        // ResolverActivity.
        // We typically want to show "Open with..." if it's a specific app.

        val packageName = resolveInfo.activityInfo.packageName
        if (packageName != "android" && packageName != "com.android.internal.app") {
          icon = resolveInfo.loadIcon(pm)
          val appName = resolveInfo.loadLabel(pm).toString()
          label = "用 $appName 打开"
        } else {
          // System resolver, just say "Open Link"
          label = "打开链接"
        }
      } else {
        label = "打开链接"
      }

      val type = if (url.contains("://")) ScanType.SCHEME else ScanType.URL

      return ScanParsedResult(
        rawValue = url,
        type = type,
        icon = icon, // If null, UI shows default link icon
        label = label,
        intent = intent,
      )
    } catch (e: Exception) {
      // Fallback to text if parsing fails
      return ScanParsedResult(
        rawValue = url,
        type = ScanType.TEXT,
        icon = null,
        label = "显示内容",
        intent = null,
      )
    }
  }
}
