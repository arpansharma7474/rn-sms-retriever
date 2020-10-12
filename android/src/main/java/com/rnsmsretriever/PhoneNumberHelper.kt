package com.rnsmsretriever

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.common.api.GoogleApiClient
import com.rnsmsretriever.GooglePlayServicesHelper.hasSupportedVersion
import com.rnsmsretriever.GooglePlayServicesHelper.isAvailable


class PhoneNumberHelper {
  private var mGoogleApiClient: GoogleApiClient? = null
  private var mPromise: Promise? = null
  private var mListener: Listener? = null

  fun setListener(listener: Listener) {
    mListener = listener
  }

  fun requestPhoneNumber(context: Context, activity: Activity?, promise: Promise?) {
    if (promise == null) {
      callAndResetListener()
      return
    }
    mPromise = promise
    if (!isAvailable(context)) {
      promiseReject(GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE, GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE)
      callAndResetListener()
      return
    }
    if (!hasSupportedVersion(context)) {
      promiseReject(GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE, GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE)
      callAndResetListener()
      return
    }
    if (activity == null) {
      promiseReject(ACTIVITY_NULL_ERROR_TYPE, ACTIVITY_NULL_ERROR_MESSAGE)
      callAndResetListener()
      return
    }
    val request = HintRequest.Builder()
      .setPhoneNumberIdentifierSupported(true)
      .build()
    val googleApiClient = getGoogleApiClient(context, activity)
    val intent = Auth.CredentialsApi
      .getHintPickerIntent(googleApiClient, request)
    try {
      activity.startIntentSenderForResult(intent.intentSender,
        REQUEST_PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0)
    } catch (e: SendIntentException) {
      promiseReject(SEND_INTENT_ERROR_TYPE, SEND_INTENT_ERROR_MESSAGE)
      callAndResetListener()
    }
  }

  //endregion
  //region - Privates
  private fun getGoogleApiClient(context: Context, activity: Activity): GoogleApiClient {
    if (mGoogleApiClient == null) {
      var builder = GoogleApiClient.Builder(context)
      builder = builder.addConnectionCallbacks(mApiClientConnectionCallbacks)
      builder = builder.addApi(Auth.CREDENTIALS_API)
      if (activity is FragmentActivity) {
        builder = builder.enableAutoManage(activity, mApiClientOnConnectionFailedListener)
      }
      mGoogleApiClient = builder.build()
    }
    return mGoogleApiClient!!
  }

  private fun callAndResetListener() {
    if (mListener != null) {
      mListener!!.phoneNumberResultReceived()
      mListener = null
    }
  }

  //endregion
  //region - Promises
  private fun promiseResolve(value: Any) {
    if (mPromise != null) {
      mPromise!!.resolve(value)
      mPromise = null
    }
  }

  private fun promiseReject(type: String, message: String) {
    if (mPromise != null) {
      mPromise!!.reject(type, message)
      mPromise = null
    }
  }

  //endregion
  //region - Callbacks and Listeners
  private val mApiClientConnectionCallbacks: GoogleApiClient.ConnectionCallbacks = object : GoogleApiClient.ConnectionCallbacks {
    override fun onConnected(bundle: Bundle?) {}
    override fun onConnectionSuspended(i: Int) {
      promiseReject(CONNECTION_SUSPENDED_ERROR_TYPE, CONNECTION_SUSPENENDED_ERROR_MESSAGE)
      callAndResetListener()
    }
  }
  private val mApiClientOnConnectionFailedListener = GoogleApiClient.OnConnectionFailedListener {
    promiseReject(CONNECTION_FAILED_ERROR_TYPE, CONNECTION_FAILED_ERROR_MESSAGE)
    callAndResetListener()
  }

  //region - Package Access
  val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent) {
      super.onActivityResult(activity, requestCode, resultCode, data)
      if (requestCode == REQUEST_PHONE_NUMBER_REQUEST_CODE) {
        if (resultCode == Activity.RESULT_OK) {
          val credential: Credential = data.getParcelableExtra(Credential.EXTRA_KEY)
          val phoneNumber = credential.id
          promiseResolve(phoneNumber)
          callAndResetListener()
          return
        }
      }
      promiseReject(ACTIVITY_RESULT_NOOK_ERROR_TYPE, ACTIVITY_RESULT_NOOK_ERROR_MESSAGE)
      callAndResetListener()
    }
  }

  //endregion
  //region - Classes
  interface Listener {
    fun phoneNumberResultReceived()
  } //endregion

  companion object {
    private const val REQUEST_PHONE_NUMBER_REQUEST_CODE = 1
    private const val ACTIVITY_NULL_ERROR_TYPE = "ACTIVITY_NULL_ERROR_TYPE"
    private const val ACTIVITY_RESULT_NOOK_ERROR_TYPE = "ACTIVITY_RESULT_NOOK_ERROR_TYPE"
    private const val CONNECTION_SUSPENDED_ERROR_TYPE = "CONNECTION_SUSPENDED_ERROR_TYPE"
    private const val CONNECTION_FAILED_ERROR_TYPE = "CONNECTION_FAILED_ERROR_TYPE"
    private const val SEND_INTENT_ERROR_TYPE = "SEND_INTENT_ERROR_TYPE"
    private const val ACTIVITY_NULL_ERROR_MESSAGE = "Activity is null."
    private const val ACTIVITY_RESULT_NOOK_ERROR_MESSAGE = "There was an error trying to get the phone number."
    private const val CONNECTION_SUSPENENDED_ERROR_MESSAGE = "Client is temporarily in a disconnected state."
    private const val CONNECTION_FAILED_ERROR_MESSAGE = "There was an error connecting the client to the service."
    private const val SEND_INTENT_ERROR_MESSAGE = "There was an error trying to send intent."
  }
}
