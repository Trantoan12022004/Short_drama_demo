package com.demo.miniflicks.dramaseries.shortstory.model

// Map EpisodeEntityOscar (Room) sang WatchedEpisodeOscar (UI model dùng cho PlaylistAdapterOscar).
// Yêu cầu truyền lang để resolve tên phim và poster đúng ngôn ngữ hiện tại.
fun EpisodeEntityOscar.toWatchedEpisode(lang: String): WatchedEpisodeOscar {
    return WatchedEpisodeOscar(
        episodeId = id,
        movieId = movieId,
        movieTitle = resolveTitle(lang),
        posterUrl = resolvePoster(lang),
        episodeNumber = episodeNumber,
        totalEpisode = totalEpisode,
        viewCount = viewCount,
        isFavorite = isFavorite,
        favoritedAt = favoritedAt,
        lastWatchedAt = lastWatchedAt
    )
}