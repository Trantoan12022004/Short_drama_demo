package com.demo.miniflicks.dramaseries.shortstory.util
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics


object EventTracking {

    fun logEvent(context: Context?, nameEvent: String?) {
        if (context != null) {
            val bundleOscar = Bundle()
            bundleOscar.putString(nameEvent, nameEvent)
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent!!, bundleOscar)
        }
    }

    fun logEvent(context: Context?, nameEvent: String?, param: String?, value: String?) {
        if (context != null) {
            val bundleOscar = Bundle()
            bundleOscar.putString(param, value)
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent!!, bundleOscar)
        }
    }

    fun logEvent(context: Context?, nameEvent: String?, param: String?, value: Long) {
        if (context != null) {
            val bundleOscar = Bundle()
            bundleOscar.putLong(param, value)
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent!!, bundleOscar)
        }
    }

    fun logEvent(context: Context?, nameEvent: String?, bundle: Bundle?) {
        if (context != null) {
            FirebaseAnalytics.getInstance(context).logEvent(nameEvent!!, bundle)
        }
    }

}