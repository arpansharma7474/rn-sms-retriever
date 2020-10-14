package com.rnsmsretriever

import android.content.pm.PackageManager.NameNotFoundException
import com.facebook.react.bridge.*
import java.util.*


class RnSmsRetrieverModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  private val mPhoneNumberHelper by lazy {
    PhoneNumberHelper()
  }

  private val mSmsHelper by lazy {
    SMSHelper(reactContext)
  }

  override fun getName(): String {
    return "RnSmsRetriever"
  }

  override fun getConstants(): MutableMap<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["SMS_EVENT"] = "me.furtado.smsretriever:SmsEvent"
    return constants
  }

  // Example method
  // See https://facebook.github.io/react-native/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Int, b: Int, promise: Promise) {
    promise.resolve(a * b)
  }

  @ReactMethod
  fun startSmsRetriever(promise: Promise?) {
    mSmsHelper.startRetriever(promise)
  }

  @ReactMethod
  fun getAppHash(promise: Promise?) {
    try {
      val a = AppSignatureHelper(reactApplicationContext)
      val signature = a.appSignatures
      promise?.resolve(signature[0])
    } catch (e: NameNotFoundException) {
      promise?.reject(e)
    }
  }

  @ReactMethod
  fun requestPhoneNumber(promise: Promise?) {
    val context = reactApplicationContext
    val activity = currentActivity
    val eventListener: ActivityEventListener = mPhoneNumberHelper.activityEventListener
    context.addActivityEventListener(eventListener)
    mPhoneNumberHelper.setListener(object : PhoneNumberHelper.Listener {
      override fun phoneNumberResultReceived() {
        context.removeActivityEventListener(eventListener)
      }
    })
    mPhoneNumberHelper.requestPhoneNumber(context, activity, promise)
  }

}
