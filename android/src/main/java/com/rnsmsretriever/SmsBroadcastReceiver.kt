package com.rnsmsretriever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status


class SmsBroadcastReceiver(private val mContext: ReactApplicationContext?) : BroadcastReceiver() {

  constructor() : this(null)

  override fun onReceive(context: Context, intent: Intent) {
    if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
      val extras = intent.extras
      if (extras == null) {
        emitJSEvent(EXTRAS_KEY, EXTRAS_NULL_ERROR_MESSAGE)
        return
      }
      val status = extras[SmsRetriever.EXTRA_STATUS] as Status
      if (status == null) {
        emitJSEvent(STATUS_KEY, STATUS_NULL_ERROR_MESSAGE)
        return
      }
      when (status.statusCode) {
        CommonStatusCodes.SUCCESS -> {
          val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String
          emitJSEvent(MESSAGE_KEY, message)
        }
        CommonStatusCodes.TIMEOUT -> {
          emitJSEvent(TIMEOUT_KEY, TIMEOUT_ERROR_MESSAGE)
        }
      }
    }
  }

  //region - Privates
  private fun emitJSEvent(key: String, message: String) {
    if (mContext == null) {
      return
    }
    if (!mContext.hasActiveCatalystInstance()) {
      return
    }
    val map = WritableNativeMap()
    map.putString(key, message)
    mContext.getJSModule(RCTDeviceEventEmitter::class.java).emit(SMS_EVENT, map)
  } //endregion

  companion object {
    private const val SMS_EVENT = "me.furtado.smsretriever:SmsEvent"
    private const val EXTRAS_KEY = "extras"
    private const val MESSAGE_KEY = "message"
    private const val STATUS_KEY = "status"
    private const val TIMEOUT_KEY = "timeout"
    private const val EXTRAS_NULL_ERROR_MESSAGE = "Extras is null."
    private const val STATUS_NULL_ERROR_MESSAGE = "Status is null."
    private const val TIMEOUT_ERROR_MESSAGE = "Timeout error."
  }

}
