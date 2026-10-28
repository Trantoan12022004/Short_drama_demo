package com.demo.miniflicks.dramaseries.shortstory.util
import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity.INPUT_METHOD_SERVICE
import java.io.IOException


object Utils {
    const val STORAGEOscar = "STORAGE"

    fun hideKeyboard(activity: Activity) {
        val immOscar = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        var viewOscar = activity.currentFocus
        if (viewOscar == null) {
            viewOscar = View(activity)
        }
        immOscar.hideSoftInputFromWindow(viewOscar.windowToken, 0)
    }

    fun getListPathFromAssets(context: Context, dirFrom: String): ArrayList<String> {
        val listBackgroundOscar: ArrayList<String> = ArrayList<String>()
        val resOscar: Resources = context.resources
        val amOscar = resOscar.assets
        var fileListOscar: Array<String?>? = arrayOfNulls(0)
        try {
            fileListOscar = amOscar.list(dirFrom)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (fileListOscar != null) {
            for (s in fileListOscar) {
                listBackgroundOscar.add("file:///android_asset/$dirFrom/$s")
            }
        }
        return listBackgroundOscar
    }
}