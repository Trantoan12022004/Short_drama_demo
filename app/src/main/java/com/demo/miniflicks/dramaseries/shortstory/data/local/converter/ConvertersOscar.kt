package com.demo.miniflicks.dramaseries.shortstory.data.local.converter
import androidx.room.TypeConverter
import com.demo.miniflicks.dramaseries.shortstory.api.LocaleTextResponseOscar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class ConvertersOscar {
    private val gsonOscar = Gson()

    // Chuyển đổi danh sách LocaleTextResponseOscar sang chuỗi JSON để lưu trong Room
    @TypeConverter
    fun fromLocaleTextListOscar(valueOscar: List<LocaleTextResponseOscar>?): String? {
        if (valueOscar == null) {
            return null
        }
        return gsonOscar.toJson(valueOscar)
    }

    // Chuyển đổi chuỗi JSON từ Room ngược lại thành List<LocaleTextResponseOscar>
    @TypeConverter
    fun toLocaleTextListOscar(valueOscar: String?): List<LocaleTextResponseOscar>? {
        if (valueOscar.isNullOrBlank()) {
            return null
        }
        val typeOscar = object : TypeToken<List<LocaleTextResponseOscar>>() {}.type
        return gsonOscar.fromJson(valueOscar, typeOscar)
    }
}