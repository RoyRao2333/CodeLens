package com.royrao.codelens

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.quicksettings.TileService

/** 快速设置磁贴服务。用户点击后直接启动 MainActivity。 */
class ScanTileService : TileService() {

    override fun onClick() {
        super.onClick()

        // 点击后收起通知栏并启动 Activity
        val intent =
            Intent(this, MainActivity::class.java).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

        if (Build.VERSION.SDK_INT >= 34) {
            // Android 14+ 推荐使用 startActivityAndCollapse pendingIntent
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
                )
            try {
                startActivityAndCollapse(pendingIntent)
            } catch (e: Exception) {
                // Fallback for some edge cases
                startActivityAndCollapse(intent)
            }
        } else {
            // Android 14 以下
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        // 可以更新 Tile 状态，例如是否可用
        qsTile.state = android.service.quicksettings.Tile.STATE_ACTIVE
        qsTile.updateTile()
    }
}
