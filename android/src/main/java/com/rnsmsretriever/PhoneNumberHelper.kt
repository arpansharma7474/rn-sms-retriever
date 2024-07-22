package com.rnsmsretriever

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import com.facebook.react.bridge.ActivityEventListener
import com.facebook.react.bridge.BaseActivityEventListener
import com.facebook.react.bridge.Promise
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.rnsmsretriever.GooglePlayServicesHelper.hasSupportedVersion
import com.rnsmsretriever.GooglePlayServicesHelper.isAvailable


class PhoneNumberHelper {
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

    val hintRequest = GetPhoneNumberHintIntentRequest.builder().build()
    val intentTask = Identity.getSignInClient(activity).getPhoneNumberHintIntent(hintRequest)

    intentTask.addOnSuccessListener { pendingIntent ->
      try {
        activity.startIntentSenderForResult(pendingIntent.intentSender,
          REQUEST_PHONE_NUMBER_REQUEST_CODE, null, 0, 0, 0)
      } catch (e: SendIntentException) {
        promiseReject(SEND_INTENT_ERROR_TYPE, SEND_INTENT_ERROR_MESSAGE)
        callAndResetListener()
      }
    }

    intentTask.addOnFailureListener { e ->
      promiseReject(SEND_INTENT_ERROR_TYPE, e.message ?: SEND_INTENT_ERROR_MESSAGE)
      callAndResetListener()
    }
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

  //region - Package Access
  val activityEventListener: ActivityEventListener = object : BaseActivityEventListener() {
    override fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?) {
      super.onActivityResult(activity, requestCode, resultCode, data)
      if (requestCode == REQUEST_PHONE_NUMBER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
        val phoneNumber = Identity.getSignInClient(activity).getPhoneNumberFromIntent(data)
        if (phoneNumber == null) {
          promiseReject(ACTIVITY_RESULT_NOOK_ERROR_TYPE, ACTIVITY_RESULT_NOOK_ERROR_MESSAGE)
          callAndResetListener()
          return
        }
        promiseResolve(phoneNumber)
        callAndResetListener()
        return
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
