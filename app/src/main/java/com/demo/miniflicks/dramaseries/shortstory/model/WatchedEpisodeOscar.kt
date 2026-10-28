package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.PrimaryKey


data class WatchedEpisodeOscar(
    // ID của tập phim (Khóa chính)
    @PrimaryKey val episodeId: Long,
    // ID của bộ phim chứa tập này
    val movieId: Long,
    // Tên bộ phim
    val movieTitle: String,
    // URL ảnh poster / thumbnail của tập phim
    val posterUrl: String,
    // Số thứ tự của tập (ví dụ: tập 1, 2, 3...)
    val episodeNumber: Int,
    // Tổng số tập của bộ phim
    val totalEpisode: Int,
    // Lượt xem của tập hoặc phim
    val viewCount: Long,
    // Trạng thái yêu thích của tập phim này
    val isFavorite: Boolean = false,
    // Thời điểm thêm vào yêu thích
    val favoritedAt: Long = 0L,
    // Thời gian xem gần nhất để sắp xếp
    val lastWatchedAt: Long = System.currentTimeMillis()
)