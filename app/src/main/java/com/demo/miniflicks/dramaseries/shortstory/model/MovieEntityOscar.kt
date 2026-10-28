package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.demo.miniflicks.dramaseries.shortstory.api.LocaleTextResponseOscar


// Bảng catalog phim đầy đủ - nguồn dữ liệu chính cho toàn bộ màn Home.
// Lưu toàn bộ danh sách đa ngôn ngữ (names, covers) từ API để resolve đúng ngôn ngữ khi hiển thị.
@Entity(tableName = "movies")
data class MovieEntityOscar(
    @PrimaryKey val id: Long,
    val legacyId: String,
    // Toàn bộ tên phim theo từng ngôn ngữ - resolve khi đọc ra UI theo codeLang hiện tại
    val names: List<LocaleTextResponseOscar>,
    // Toàn bộ URL poster theo từng ngôn ngữ - resolve khi đọc ra UI theo codeLang hiện tại
    val covers: List<LocaleTextResponseOscar>,
    val rating: Float,
    val viewCount: Long,
    val searchCount: Long,
    val totalEpisode: Int,
    val subtitle: Boolean,
    val firstEpisodeId: Long?,
    // Cờ đánh dấu phim thuộc mục nào trên Home, được set khi đồng bộ từ từng API tương ứng
    val isTrending: Boolean = false,
    val isNewRelease: Boolean = false,
    val isSpotlight: Boolean = false,
    val isTopSearch: Boolean = false,
    // Giữ thứ tự trả về gốc của mỗi API để hiển thị đúng thứ tự trên RecyclerView
    val trendingOrder: Int = Int.MAX_VALUE,
    val newReleaseOrder: Int = Int.MAX_VALUE,
    val spotlightOrder: Int = Int.MAX_VALUE,
    val syncedAt: Long = System.currentTimeMillis()
)

// Resolve tên phim theo ngôn ngữ ưu tiên, fallback sang ngôn ngữ đầu tiên nếu không có
fun MovieEntityOscar.resolveTitle(lang: String): String {
    val matchedOscar = names.firstOrNull { it.languageCode == lang }?.value
    if (matchedOscar != null) {
        return matchedOscar
    }
    return names.firstOrNull()?.value ?: ""
}

// Resolve URL poster theo ngôn ngữ ưu tiên, fallback sang ngôn ngữ đầu tiên nếu không có
fun MovieEntityOscar.resolvePoster(lang: String): String {
    val matchedOscar = covers.firstOrNull { it.languageCode == lang }?.value
    if (matchedOscar != null) {
        return matchedOscar
    }
    return covers.firstOrNull()?.value ?: ""
}