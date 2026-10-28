package com.demo.miniflicks.dramaseries.shortstory.model

// UI model dùng cho PlaylistAdapterOscar, được map từ WatchedMovieOscar entity
data class PlaylistItemOscar(
    // ID phim
    val movieId: Long,
    // Tên phim
    val movieTitle: String,
    // URL ảnh poster
    val moviePoster: String,
    // Lượt xem
    val viewCount: Long,
    // Tập hiện tại đang xem
    val currentEpisode: Int,
    // Tổng số tập
    val totalEpisode: Int,
    // Trạng thái yêu thích
    val isFavorite: Boolean
)