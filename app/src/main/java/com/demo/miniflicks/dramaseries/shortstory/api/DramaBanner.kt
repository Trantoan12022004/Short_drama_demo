package com.demo.miniflicks.dramaseries.shortstory.api
import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import com.sulotion.security.NativeSecurity
import java.util.UUID


object DramaBanner {

    fun getRequestId(): String {
        return UUID.randomUUID().toString()
    }

    @SuppressLint("HardwareIds")
    fun getDeviceId(context: Context): String {
        val deviceIdOscar = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID,
        ) ?: "unknown-device"
        return deviceIdOscar
    }

    fun getCurrentTimeMillis(): Long {
        return NativeSecurity.getCurrentTimeMillis()
    }

    fun getPackageName(context: Context): String {
        return context.packageName
    }
}