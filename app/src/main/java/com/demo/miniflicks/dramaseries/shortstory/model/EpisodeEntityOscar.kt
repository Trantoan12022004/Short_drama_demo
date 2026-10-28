package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.demo.miniflicks.dramaseries.shortstory.api.LocaleTextResponseOscar


// Bảng episode - lưu toàn bộ dữ liệu metadata của tất cả các tập của phim,
// cùng trạng thái "đã xem gần đây" và "yêu thích" của người dùng.
// Lưu toàn bộ danh sách đa ngôn ngữ (movieNames, posters) để resolve đúng ngôn ngữ khi hiển thị.
@Entity(tableName = "episodes", indices = [Index("movieId")])
data class EpisodeEntityOscar(
    @PrimaryKey val id: Long,
    val movieId: Long,
    // Toàn bộ tên bộ phim theo từng ngôn ngữ - resolve khi đọc ra UI theo codeLang hiện tại
    val movieNames: List<LocaleTextResponseOscar>,
    // Toàn bộ URL poster/thumbnail theo từng ngôn ngữ - resolve khi đọc ra UI theo codeLang hiện tại
    val posters: List<LocaleTextResponseOscar>,
    val episodeNumber: Int,
    val totalEpisode: Int,
    val url: String?,
    val premium: Boolean,
    val rating: Float?,
    val viewCount: Long,
    val subtitleUrl: String?,
    // Danh sách toàn bộ phụ đề đa ngôn ngữ từ API
    val subtitles: List<LocaleTextResponseOscar>? = null,
    // Danh sách toàn bộ hình thu nhỏ từ API
    val thumbnails: List<LocaleTextResponseOscar>? = null,
    // Trạng thái yêu thích của tập phim này (tập 1 = trạng thái yêu thích của cả phim)
    val isFavorite: Boolean = false,
    // Thời điểm tập này được thêm vào danh sách yêu thích để sắp xếp tập mới thêm lên đầu
    val favoritedAt: Long = 0L,
    // Thời điểm tập này được tính là "đã xem" (đã phát > 10 giây). 0 = chưa từng xem đủ ngưỡng
    val lastWatchedAt: Long = 0L,
    // Tiến trình xem hiện tại tính bằng mili-giây
    val lastPositionMs: Long = 0L,
    // Tổng thời lượng video tính bằng mili-giây
    val durationMs: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
)

// Resolve tên phim theo ngôn ngữ ưu tiên, fallback sang ngôn ngữ đầu tiên nếu không có
fun EpisodeEntityOscar.resolveTitle(lang: String): String {
    val matchedOscar = movieNames.firstOrNull { it.languageCode == lang }?.value
    if (matchedOscar != null) {
        return matchedOscar
    }
    return movieNames.firstOrNull()?.value ?: ""
}

// Resolve URL poster theo ngôn ngữ ưu tiên, fallback sang ngôn ngữ đầu tiên nếu không có
fun EpisodeEntityOscar.resolvePoster(lang: String): String {
    val matchedOscar = posters.firstOrNull { it.languageCode == lang }?.value
    if (matchedOscar != null) {
        return matchedOscar
    }
    return posters.firstOrNull()?.value ?: ""
}