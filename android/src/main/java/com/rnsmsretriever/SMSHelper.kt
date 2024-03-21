package com.rnsmsretriever

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.content.Context;
import android.util.Log
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener


class SMSHelper(private val mContext: ReactApplicationContext) {
  private var mReceiver: BroadcastReceiver? = null
  private var mPromise: Promise? = null

  //region - Package Access
  fun startRetriever(promise: Promise?) {
    mPromise = promise
    if (!GooglePlayServicesHelper.isAvailable(mContext)) {
      promiseReject(GooglePlayServicesHelper.UNAVAILABLE_ERROR_TYPE, GooglePlayServicesHelper.UNAVAILABLE_ERROR_MESSAGE)
      return
    }
    if (!GooglePlayServicesHelper.hasSupportedVersion(mContext)) {
      promiseReject(GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_TYPE, GooglePlayServicesHelper.UNSUPORTED_VERSION_ERROR_MESSAGE)
      return
    }
    val client = SmsRetriever.getClient(mContext)
    val task = client.startSmsRetriever()
    task.addOnSuccessListener(mOnSuccessListener)
    task.addOnFailureListener(mOnFailureListener)
  }

  //endregion
  //region - Privates
  private fun tryToRegisterReceiver(): Boolean {
    mReceiver = SmsBroadcastReceiver(mContext)
    val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
    return try {
      mContext.registerReceiver(mReceiver, intentFilter, Context.RECEIVER_EXPORTED)
      true
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }

  private fun unregisterReceiverIfNeeded() {
    if (mReceiver == null) {
      return
    }
    try {
      mContext.unregisterReceiver(mReceiver)
    } catch (e: Exception) {
      e.printStackTrace()
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
  //region - Listeners
  private val mOnSuccessListener: OnSuccessListener<Void> = OnSuccessListener {
    val registered = tryToRegisterReceiver()
    promiseResolve(registered)
  }
  private val mOnFailureListener = OnFailureListener {
    unregisterReceiverIfNeeded()
    promiseReject(TASK_FAILURE_ERROR_TYPE, TASK_FAILURE_ERROR_MESSAGE)
  } //endregion

  companion object {
    private const val TASK_FAILURE_ERROR_TYPE = "TASK_FAILURE_ERROR_TYPE"
    private const val TASK_FAILURE_ERROR_MESSAGE = "Task failed."
  }

}
