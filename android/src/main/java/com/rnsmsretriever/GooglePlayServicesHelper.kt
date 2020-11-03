package com.rnsmsretriever

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import androidx.core.content.pm.PackageInfoCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


internal object GooglePlayServicesHelper {
  private const val MINIMAL_VERSION = 10200000
  const val UNAVAILABLE_ERROR_TYPE = "UNAVAILABLE_ERROR_TYPE"
  const val UNSUPORTED_VERSION_ERROR_TYPE = "UNSUPORTED_VERSION_ERROR_TYPE"
  const val UNAVAILABLE_ERROR_MESSAGE = "Google Play Services is not available."
  const val UNSUPORTED_VERSION_ERROR_MESSAGE = "The device version of Google Play Services is not supported."

  fun isAvailable(context: Context): Boolean {
    val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
    return result != ConnectionResult.SERVICE_MISSING && result != ConnectionResult.SERVICE_DISABLED && result != ConnectionResult.SERVICE_INVALID
  }

  fun hasSupportedVersion(context: Context): Boolean {
    val manager = context.packageManager
    return try {
      val version = PackageInfoCompat.getLongVersionCode(manager.getPackageInfo(GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, 0))
      version >= MINIMAL_VERSION
    } catch (e: NameNotFoundException) {
      false
    }
  }
}
