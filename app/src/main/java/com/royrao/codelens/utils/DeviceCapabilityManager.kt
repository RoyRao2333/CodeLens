package com.royrao.codelens.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/**
 * Manager to check and cache device capabilities, specifically GMS availability.
 */
object DeviceCapabilityManager {
    private const val PREF_NAME = "device_caps"
    private const val KEY_GMS_AVAILABLE = "gms_available"
    private const val KEY_LAST_CHECK_VERSION = "last_check_version"

    /**
     * Checks if GMS is available on the device using GoogleApiAvailability.
     */
    fun isGmsAvailable(context: Context): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        return resultCode == ConnectionResult.SUCCESS
    }

    /**
     * Checks capabilities and saves them to SharedPreferences.
     * Should be called on app startup.
     */
    fun checkAndSaveCapabilities(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        
        // We can optimize by only checking if OS version changed, or just check every time since it's fast enough usually?
        // Requirement said: "Startup check logic" and "persistence".
        // Let's check every time to be safe but cache result for synchronous access elsewhere.
        
        val gmsAvailable = isGmsAvailable(context)
        
        prefs.edit().apply {
            putBoolean(KEY_GMS_AVAILABLE, gmsAvailable)
            putString("manufacturer", Build.MANUFACTURER)
            putString("model", Build.MODEL)
            putInt("sdk_int", Build.VERSION.SDK_INT)
            apply()
        }
    }

    /**
     * Retrieves the cached GMS availability state.
     * Defaults to false if not checked yet.
     */
    fun getCachedGmsState(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        // If not contained, fallback to checking (though this might be on main thread)
        if (!prefs.contains(KEY_GMS_AVAILABLE)) {
            return isGmsAvailable(context)
        }
        return prefs.getBoolean(KEY_GMS_AVAILABLE, false)
    }
}
