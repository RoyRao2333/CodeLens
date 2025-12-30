package com.royrao.codelens

import android.content.Context
import android.os.Build
import androidx.core.content.edit
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * 管理设备能力检测，主要是 GMS 状态。
 * 使用 SharedPreferences 缓存检测结果，避免每次启动都调用系统 API。
 */
object DeviceCapabilityManager {
    private const val PREFS_NAME = "device_specs"
    private const val KEY_GMS_AVAILABLE = "gms_enabled"
    private const val KEY_LAST_CHECK_VERSION = "check_version"

    /**
     * 检查 GMS 是否可用。优先读取缓存。
     */
    fun isGmsAvailable(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // 如果缓存存在且当前 App 版本未变（可选，这里简单起见只依赖缓存），直接返回
        if (prefs.contains(KEY_GMS_AVAILABLE)) {
            return prefs.getBoolean(KEY_GMS_AVAILABLE, false)
        }

        // 执行检测
        val status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        val isAvailable = (status == ConnectionResult.SUCCESS)

        // 写入缓存，同时记录一些机型信息备查
        prefs.edit {
            putBoolean(KEY_GMS_AVAILABLE, isAvailable)
            putString("manufacturer", Build.MANUFACTURER)
            putString("model", Build.MODEL)
            putInt("android_sdk", Build.VERSION.SDK_INT)
        }
        
        return isAvailable
    }
}
