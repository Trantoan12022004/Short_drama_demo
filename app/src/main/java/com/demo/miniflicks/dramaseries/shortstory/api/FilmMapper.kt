package com.demo.miniflicks.dramaseries.shortstory.api
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import java.math.BigDecimal


object FilmMapper {
    // Helper: resolve 1 giá trị text theo ngôn ngữ ưu tiên, fallback sang ngôn ngữ đầu tiên
    private fun List<LocaleTextResponseOscar>.pickValue(preferredLang: String = "en"): String {
        val matchedOscar = firstOrNull { it.languageCode == preferredLang }?.value
        if (matchedOscar != null) {
            return matchedOscar
        }
        return firstOrNull()?.value ?: ""
    }

    // Map FilmListResponseOscar sang MovieCardOscar để hiển thị trực tiếp (không qua DB)
    fun FilmListResponseOscar.toMovieCard(preferredLang: String = "en"): MovieCardOscar {
        return MovieCardOscar(
            id = id,
            title = names.pickValue(preferredLang),
            posterUrl = covers.pickValue(preferredLang),
            rating = rating.toFloat(),
            viewCount = viewCount,
            badgeText = categories.firstOrNull()?.slug
        )
    }

    // Map FilmListResponseOscar sang MovieEntityOscar để lưu vào Room.
    // KHÔNG resolve lang - lưu thẳng toàn bộ list đa ngôn ngữ từ API.
    fun FilmListResponseOscar.toMovieEntity(): MovieEntityOscar {
        return MovieEntityOscar(
            id = id,
            legacyId = legacyId,
            names = names,
            covers = covers,
            rating = rating.toFloat(),
            viewCount = viewCount,
            searchCount = searchCount,
            totalEpisode = totalEpisode,
            subtitle = subtitle,
            firstEpisodeId = firstEpisode.id
        )
    }

    // Map GenreResponseOscar sang CategoryEntityOscar để lưu vào Room.
    // KHÔNG resolve lang - lưu thẳng toàn bộ list translations từ API.
    fun GenreResponseOscar.toCategoryEntity(): CategoryEntityOscar? {
        val categoryIdOscar = id
        if (categoryIdOscar == null) {
            return null
        }
        val resolvedSlugOscar: String
        if (slug != null) {
            resolvedSlugOscar = slug
        } else {
            resolvedSlugOscar = ""
        }
        // Nếu không có translations, tạo 1 entry giả từ slug để UI vẫn hiển thị được
        val resolvedTranslationsOscar: List<LocaleTextResponseOscar>
        if (!translations.isNullOrEmpty()) {
            resolvedTranslationsOscar = translations
        } else {
            resolvedTranslationsOscar = listOf(LocaleTextResponseOscar(value = resolvedSlugOscar))
        }
        return CategoryEntityOscar(
            id = categoryIdOscar,
            slug = resolvedSlugOscar,
            translations = resolvedTranslationsOscar
        )
    }

    // Map tập đầu tiên (firstEpisode) của 1 phim trong danh sách sang EpisodeEntityOscar.
    // Được gọi ngay khi đồng bộ catalog để phim nào cũng có sẵn 1 row episode-1 trong Room,
    // nhờ vậy user có thể bấm yêu thích 1 phim ngay từ Home mà không cần mở chi tiết phim trước.
    // KHÔNG resolve lang - lưu thẳng toàn bộ list đa ngôn ngữ từ API.
    fun FilmListResponseOscar.toFirstEpisodeEntity(): EpisodeEntityOscar? {
        val segmentOscar = firstEpisode
        val episodeIdOscar = segmentOscar.id
        if (episodeIdOscar == null) {
            return null
        }

        // Resolve số tập: server trả về 0-based index, UI hiển thị 1-based
        val resolvedEpisodeNumberOscar: Int
        if (segmentOscar.episodeNumber != null) {
            resolvedEpisodeNumberOscar = segmentOscar.episodeNumber + 1
        } else {
            resolvedEpisodeNumberOscar = 1
        }

        val resolvedViewCountOscar: Long
        if (segmentOscar.viewCount != null) {
            resolvedViewCountOscar = segmentOscar.viewCount
        } else {
            resolvedViewCountOscar = viewCount
        }

        // Poster tập: ưu tiên thumbnail riêng, nếu không có dùng covers của phim
        val resolvedPostersOscar: List<LocaleTextResponseOscar>
        val thumbnailsOscar = segmentOscar.thumbnails
        if (!thumbnailsOscar.isNullOrEmpty()) {
            resolvedPostersOscar = thumbnailsOscar
        } else {
            resolvedPostersOscar = covers
        }

        return EpisodeEntityOscar(
            id = episodeIdOscar,
            movieId = id,
            movieNames = names,
            posters = resolvedPostersOscar,
            episodeNumber = resolvedEpisodeNumberOscar,
            totalEpisode = totalEpisode,
            url = segmentOscar.url,
            premium = segmentOscar.premium ?: false,
            rating = segmentOscar.rating?.toFloat(),
            viewCount = resolvedViewCountOscar,
            subtitleUrl = resolveSubtitleUrlOscar(segmentOscar),
            subtitles = segmentOscar.subtitles,
            thumbnails = segmentOscar.thumbnails
        )
    }

    // Map 1 tập phim trong màn chi tiết phim (FilmDetailResponseOscar.episodes) sang EpisodeEntityOscar.
    // Trả về null nếu server không có id cho tập hoặc phim (không đủ dữ liệu để cache).
    // KHÔNG resolve lang - lưu thẳng toàn bộ list đa ngôn ngữ từ API.
    fun toEpisodeEntity(detail: FilmDetailResponseOscar, segment: SegmentResponseOscar): EpisodeEntityOscar? {
        val episodeIdOscar = segment.id
        val movieIdOscar = detail.id
        if (episodeIdOscar == null || movieIdOscar == null) {
            return null
        }

        // Lưu toàn bộ names đa ngôn ngữ, nếu không có thì tạo list rỗng
        val movieNameListOscar: List<LocaleTextResponseOscar>
        if (!detail.names.isNullOrEmpty()) {
            movieNameListOscar = detail.names
        } else {
            movieNameListOscar = emptyList()
        }

        // Poster tập: ưu tiên thumbnail riêng, nếu không có dùng covers của phim
        val resolvedPostersOscar: List<LocaleTextResponseOscar>
        val thumbnailsOscar = segment.thumbnails
        if (!thumbnailsOscar.isNullOrEmpty()) {
            resolvedPostersOscar = thumbnailsOscar
        } else if (!detail.covers.isNullOrEmpty()) {
            resolvedPostersOscar = detail.covers
        } else {
            resolvedPostersOscar = emptyList()
        }

        val totalEpisodeOscar: Int
        if (detail.totalEpisode != null) {
            totalEpisodeOscar = detail.totalEpisode
        } else {
            totalEpisodeOscar = 0
        }

        return EpisodeEntityOscar(
            id = episodeIdOscar,
            movieId = movieIdOscar,
            movieNames = movieNameListOscar,
            posters = resolvedPostersOscar,
            episodeNumber = (segment.episodeNumber ?: 0) + 1,
            totalEpisode = totalEpisodeOscar,
            url = segment.url,
            premium = segment.premium ?: false,
            rating = segment.rating?.toFloat(),
            viewCount = segment.viewCount ?: 0L,
            subtitleUrl = resolveSubtitleUrlOscar(segment),
            subtitles = segment.subtitles,
            thumbnails = segment.thumbnails
        )
    }

    // Chuyển đổi từ EpisodeEntityOscar trong Room sang SegmentResponseOscar cho Player UI
    fun EpisodeEntityOscar.toSegmentResponse(): SegmentResponseOscar {
        val ratingBigDecimalOscar: BigDecimal?
        if (rating != null) {
            ratingBigDecimalOscar = BigDecimal(rating.toString())
        } else {
            ratingBigDecimalOscar = null
        }
        return SegmentResponseOscar(
            id = id,
            legacyId = null,
            episodeNumber = episodeNumber - 1,
            url = url,
            premium = premium,
            rating = ratingBigDecimalOscar,
            viewCount = viewCount,
            thumbnails = thumbnails,
            subtitles = subtitles
        )
    }

    // Chuyển danh sách EpisodeEntityOscar từ Room DB thành FilmDetailResponseOscar cho PlayVideoActivityOscar.
    // Resolve tên/poster theo ngôn ngữ hiện tại khi build response.
    fun buildFilmDetailFromEntities(
        movieId: Long,
        entities: List<EpisodeEntityOscar>,
        preferredLang: String
    ): FilmDetailResponseOscar? {
        if (entities.isEmpty()) {
            return null
        }
        val firstItemOscar = entities[0]
        val segmentListOscar = mutableListOf<SegmentResponseOscar>()
        for (item in entities) {
            segmentListOscar.add(item.toSegmentResponse())
        }
        val totalOscar = firstItemOscar.totalEpisode
        val resolvedTitleOscar = firstItemOscar.movieNames.pickValue(preferredLang)
        val resolvedPosterOscar = firstItemOscar.posters.pickValue(preferredLang)
        return FilmDetailResponseOscar(
            id = movieId,
            names = listOf(LocaleTextResponseOscar(value = resolvedTitleOscar)),
            covers = listOf(LocaleTextResponseOscar(value = resolvedPosterOscar)),
            totalEpisode = totalOscar,
            episodes = segmentListOscar
        )
    }

    // Map ngược MovieEntityOscar (đọc từ Room) sang MovieCardOscar để tái sử dụng adapter UI hiện có.
    // Yêu cầu truyền lang để resolve tên/poster theo ngôn ngữ hiện tại.
    fun MovieEntityOscar.toMovieCard(lang: String, isFavorite: Boolean = false): MovieCardOscar {
        val resolvedTitleOscar = names.pickValue(lang)
        val resolvedPosterOscar = covers.pickValue(lang)
        return MovieCardOscar(
            id = id,
            title = resolvedTitleOscar,
            posterUrl = resolvedPosterOscar,
            rating = rating,
            viewCount = viewCount,
            badgeText = null,
            isFavorite = isFavorite
        )
    }

    // Lấy URL phụ đề của tập: subtitleUrl từ subtitles list (không cần lang vì đây là URL để ExoPlayer tự xử lý)
    private fun resolveSubtitleUrlOscar(segmentOscar: SegmentResponseOscar): String? {
        val subtitlesOscar = segmentOscar.subtitles
        if (subtitlesOscar.isNullOrEmpty()) {
            return null
        }
        // Lấy URL đầu tiên có giá trị
        for (subtitleOscar in subtitlesOscar) {
            val valueOscar = subtitleOscar.value
            if (!valueOscar.isNullOrBlank()) {
                return valueOscar
            }
        }
        return null
    }
}