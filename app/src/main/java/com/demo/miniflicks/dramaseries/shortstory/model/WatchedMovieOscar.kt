package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "watched_movies")
data class WatchedMovieOscar(
    @PrimaryKey val movieId: Long,        // ID phim
    val movieTitle: String,                // Tên phim
    val moviePoster: String,               // URL ảnh poster
    val viewCount: Long,                   // Lượt xem (hiển thị trên item)
    val currentEpisode: Int,               // Tập đang xem hiện tại
    val totalEpisode: Int,                 // Tổng số tập
    val isFavorite: Boolean = false,       // Đánh dấu yêu thích
    val lastWatchedAt: Long = System.currentTimeMillis() // Thời gian xem gần nhất (để sort)
)