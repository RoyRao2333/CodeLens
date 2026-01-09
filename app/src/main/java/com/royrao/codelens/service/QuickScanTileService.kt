package com.royrao.codelens.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.royrao.codelens.MainActivity

class QuickScanTileService : TileService() {
  companion object {
    const val ACTION_QUICK_SCAN = "com.royrao.codelens.ACTION_QUICK_SCAN"
  }

  override fun onClick() {
    super.onClick()

    val intent =
      Intent(this, MainActivity::class.java).apply {
        action = ACTION_QUICK_SCAN
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(
          Intent.FLAG_ACTIVITY_CLEAR_TOP
        ) // Ensure we potentially clear top to handle new intent properly
      }

    if (Build.VERSION.SDK_INT >= 34) {
      val pendingIntent =
        PendingIntent.getActivity(
          this,
          0,
          intent,
          PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
      startActivityAndCollapse(pendingIntent)
    } else {
      @Suppress("DEPRECATION") startActivityAndCollapse(intent)
    }
  }
}
