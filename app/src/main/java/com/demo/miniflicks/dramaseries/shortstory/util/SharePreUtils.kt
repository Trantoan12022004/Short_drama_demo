package com.demo.miniflicks.dramaseries.shortstory.util
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.google.gson.Gson


class SharedPreUtilsOscar {

    companion object {
        private const val PREF_PERMISSION_CLICKOscar = "IS_COUNT_PERMISSION_CLICK"
        private const val KEY_NOTIFICATION_PERMISSION_CLICKOscar = "NOTIFICATION_PERMISSION_CLICK"
        private const val KEY_MEMORY_PERMISSION_CLICKOscar = "MEMORY_PERMISSION_CLICK"

        var mSharePrefOscar: SharedPreferences? = null
        fun init(context: Context) {
            if (mSharePrefOscar == null) {
                mSharePrefOscar = PreferenceManager.getDefaultSharedPreferences(context)
            }
        }

        @SuppressLint("StaticFieldLeak")
        private var instanceOscar: SharedPreUtilsOscar? = null
        fun getInstance(): SharedPreUtilsOscar {
            if (instanceOscar == null) {
                instanceOscar = SharedPreUtilsOscar()
            }
            return instanceOscar as SharedPreUtilsOscar
        }
    }

    fun clearAll() {
        mSharePrefOscar?.let {
            it.edit().clear().apply()
        }
    }

    fun removeValueWithKey(key: String) {
        mSharePrefOscar?.let {
            it.edit().remove(key).commit()
        }
    }

    fun setString(key: String, value: String) {
        mSharePrefOscar?.let {
            it.edit().putString(key, value).apply()
        }
    }

    fun getString(key: String, value: String): String? {
        mSharePrefOscar?.let {
            return it.getString(key, value)
        }
        return null
    }

    fun setInt(key: String, value: Int) {
        mSharePrefOscar?.let {
            it.edit().putInt(key, value).apply()
        }
    }

    fun getInt(key: String, value: Int): Int {
        mSharePrefOscar?.let {
            return it.getInt(key, value)
        }
        return -1
    }

    fun setLong(key: String, value: Long) {
        mSharePrefOscar?.let {
            it.edit().putLong(key, value).apply()
        }
    }

    fun getLong(key: String, value: Long): Long {
        mSharePrefOscar?.let {
            return it.getLong(key, value)
        }
        return -1
    }

    fun setBoolean(key: String, value: Boolean) {
        mSharePrefOscar?.let {
            it.edit().putBoolean(key, value).apply()
        }
    }

    fun getBoolean(key: String, value: Boolean): Boolean {
        return mSharePrefOscar?.getBoolean(key, value) ?: value
    }

    fun isRated(context: Context): Boolean {
        val preOscar = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        return preOscar.getBoolean("rated", false)
    }

    fun forceRated(context: Context) {
        val preOscar = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        val editorOscar = preOscar.edit()
        editorOscar.putBoolean("rated", true)
        editorOscar.apply()
    }

    fun isFirstApp(context: Context): Boolean {
        val preOscar = context.getSharedPreferences("IS_FIRST_LANGUAGE", Context.MODE_PRIVATE)
        return preOscar.getBoolean("IS_FIRST_APP", false)
    }

    fun setFirstApp(context: Context) {
        val preOscar = context.getSharedPreferences("IS_FIRST_LANGUAGE", Context.MODE_PRIVATE)
        val editorOscar = preOscar.edit()
        editorOscar.putBoolean("IS_FIRST_APP", true)
        editorOscar.apply()
    }

    fun getCountOpenApp(context: Context): Int {
        val preOscar = context.getSharedPreferences("IS_COUNT_OPEN_APP", Context.MODE_PRIVATE)
        return preOscar.getInt("IS_COUNT_OPEN_APP", 0)
    }

    fun setCountOpenApp(context: Context) {
        val preOscar = context.getSharedPreferences("IS_COUNT_OPEN_APP", Context.MODE_PRIVATE)
        val editorOscar = preOscar.edit()
        editorOscar.putInt("IS_COUNT_OPEN_APP", preOscar.getInt("IS_COUNT_OPEN_APP", 0) + 1)
        editorOscar.apply()
    }

    fun setObject(context: Context, key: String?, value: Any?) {
        if (mSharePrefOscar == null) init(context)
        val gsonOscar = Gson()
        val jsonOscar = gsonOscar.toJson(value)
        val editorOscar = mSharePrefOscar!!.edit()
        editorOscar.putString(key, jsonOscar)
        editorOscar.apply()
    }

    fun getObject(context: Context, key: String?, classObj: Class<*>?): Any {
        if (mSharePrefOscar == null) init(context)
        val gsonOscar = Gson()
        val jsonOscar = mSharePrefOscar!!.getString(key, "")
        return gsonOscar.fromJson(jsonOscar, classObj)
    }

    fun getCountPermissionClick(context: Context): Int {
        return getNotificationPermissionClickCount(context)
    }

    fun getNotificationPermissionClickCount(context: Context): Int {
        val preOscar = context.getSharedPreferences(PREF_PERMISSION_CLICKOscar, Context.MODE_PRIVATE)
        val countOscar = preOscar.getInt(KEY_NOTIFICATION_PERMISSION_CLICKOscar, 0)
        return countOscar
    }

    fun setNotificationPermissionClickCount(context: Context, count: Int) {
        val preOscar = context.getSharedPreferences(PREF_PERMISSION_CLICKOscar, Context.MODE_PRIVATE)
        val editorOscar = preOscar.edit()
        editorOscar.putInt(KEY_NOTIFICATION_PERMISSION_CLICKOscar, count)
        editorOscar.apply()
    }

    fun getMemoryPermissionClickCount(context: Context): Int {
        val preOscar = context.getSharedPreferences(PREF_PERMISSION_CLICKOscar, Context.MODE_PRIVATE)
        val countOscar = preOscar.getInt(KEY_MEMORY_PERMISSION_CLICKOscar, 0)
        return countOscar
    }

    fun setMemoryPermissionClickCount(context: Context, count: Int) {
        val preOscar = context.getSharedPreferences(PREF_PERMISSION_CLICKOscar, Context.MODE_PRIVATE)
        val editorOscar = preOscar.edit()
        editorOscar.putInt(KEY_MEMORY_PERMISSION_CLICKOscar, count)
        editorOscar.apply()
    }

}