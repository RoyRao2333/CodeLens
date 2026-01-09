package com.royrao.codelens.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

/** Manager to check and cache device capabilities, specifically GMS availability. */
object DeviceCapabilityManager {
  private const val PREF_NAME = "device_caps"
  private const val KEY_GMS_AVAILABLE = "gms_available"
  private const val KEY_LAST_CHECK_VERSION = "last_check_version"

  /** Checks if GMS is available on the device using GoogleApiAvailability. */
  fun isGmsAvailable(context: Context): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
    return resultCode == ConnectionResult.SUCCESS
  }
}
