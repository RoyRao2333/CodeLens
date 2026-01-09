package com.royrao.codelens.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService
import com.royrao.codelens.MainActivity

class ScanTileService : TileService() {

  override fun onClick() {
    super.onClick()

    val intent =
      Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

    if (Build.VERSION.SDK_INT >= 34) { // Android 14
      val pendingIntent =
        PendingIntent.getActivity(
          this,
          0,
          intent,
          PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
      startActivityAndCollapse(pendingIntent)
    } else {
      // For older versions, this is deprecated but compatible way,
      // or simply startActivityAndCollapse(intent) which was available since API 24
      // but deprecated in 34.
      @Suppress("DEPRECATION") startActivityAndCollapse(intent)
    }
  }
}
