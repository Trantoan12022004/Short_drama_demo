package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.demo.miniflicks.dramaseries.shortstory.api.LocaleTextResponseOscar


// Bảng thể loại phim (Category/Genre), đồng bộ từ /short-dram/categories.
// Lưu toàn bộ danh sách đa ngôn ngữ (translations) để resolve đúng ngôn ngữ khi hiển thị.
@Entity(tableName = "categories")
data class CategoryEntityOscar(
    @PrimaryKey val id: Long,
    val slug: String,
    // Toàn bộ tên thể loại theo từng ngôn ngữ - resolve khi đọc ra UI theo codeLang hiện tại
    val translations: List<LocaleTextResponseOscar>
)

// Resolve tên thể loại theo ngôn ngữ ưu tiên, fallback sang slug nếu không có bản dịch
fun CategoryEntityOscar.resolveName(lang: String): String {
    val matchedOscar = translations.firstOrNull { it.languageCode == lang }?.value
    if (matchedOscar != null) {
        return matchedOscar
    }
    val firstValueOscar = translations.firstOrNull()?.value
    if (!firstValueOscar.isNullOrBlank()) {
        return firstValueOscar
    }
    return slug
}