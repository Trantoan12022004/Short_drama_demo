package com.demo.miniflicks.dramaseries.shortstory.api
import android.content.Context
import java.util.Properties


class ConfigManagerOscar(private val contextOscar: Context) {
    private val propertiesOscar = Properties()

    init {
        try {
            contextOscar.assets.open("api_config.properties").use { inputStream ->
                propertiesOscar.load(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getBaseUrl(): String {
        return propertiesOscar.getProperty("base_url", "https://example.com/")
    }

    fun getApiKeyToken(): String {
        return propertiesOscar.getProperty("X_Api_Key_Token", "")
    }

    fun getApiKeyEncryption(): Boolean {
        return propertiesOscar.getProperty("api_key_encryption", "true").toBoolean()
    }

    fun getPackageName(): String {
        return propertiesOscar.getProperty("X_Package_Name", contextOscar.packageName)
    }
}