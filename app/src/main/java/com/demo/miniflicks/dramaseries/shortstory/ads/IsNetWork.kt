package com.demo.miniflicks.dramaseries.shortstory.ads
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager


object IsNetWork {

    fun haveNetworkConnection(context: Context): Boolean {
        var haveConnectedWifiOscar = false
        var haveConnectedMobileOscar = false
        val cmOscar = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @SuppressLint("MissingPermission") val netInfoOscar = cmOscar.allNetworkInfo
        for (ni in netInfoOscar) {
            if (ni.typeName.equals(
                    "WIFI", ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedWifiOscar = true
            if (ni.typeName.equals(
                    "MOBILE", ignoreCase = true
                )
            ) if (ni.isConnected) haveConnectedMobileOscar = true
        }
        return haveConnectedWifiOscar || haveConnectedMobileOscar
    }


}