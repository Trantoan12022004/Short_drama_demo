package com.demo.miniflicks.dramaseries.shortstory.api
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal


data class FilmDetailResponseOscar(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("legacyId") val legacyId: String? = null,
    @SerializedName("subtitle") val subtitle: Boolean? = null,
    @SerializedName("rating") val rating: BigDecimal? = null,
    @SerializedName("viewCount") val viewCount: Long? = null,
    @SerializedName("searchCount") val searchCount: Long? = null,
    @SerializedName("totalEpisode") val totalEpisode: Int? = null,
    @SerializedName("names") val names: List<LocaleTextResponseOscar>? = null,
    @SerializedName("covers") val covers: List<LocaleTextResponseOscar>? = null,
    @SerializedName("categories") val categories: List<GenreResponseOscar>? = null,
    @SerializedName("episodes") val episodes: List<SegmentResponseOscar>? = null
)

data class FilmListResponseOscar(
    @SerializedName("id") val id: Long,
    @SerializedName("legacyId") val legacyId: String,
    @SerializedName("subtitle") val subtitle: Boolean,
    @SerializedName("rating") val rating: BigDecimal,
    @SerializedName("viewCount") val viewCount: Long,
    @SerializedName("searchCount") val searchCount: Long,
    @SerializedName("totalEpisode") val totalEpisode: Int,
    @SerializedName("firstEpisode") val firstEpisode: SegmentResponseOscar,
    @SerializedName("names") val names: List<LocaleTextResponseOscar>,
    @SerializedName("covers") val covers: List<LocaleTextResponseOscar>,
    @SerializedName("categories") val categories: List<GenreResponseOscar>
)

data class FilmPageResponseOscar(
    @SerializedName("items") val items: List<FilmListResponseOscar> = emptyList(),
    @SerializedName("page") val page: Int = 0,
    @SerializedName("size") val size: Int = 0,
    @SerializedName("totalElements") val totalElements: Long = 0,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("first") val first: Boolean = false,
    @SerializedName("last") val last: Boolean = false
)

class GenreResponseOscar(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("slug") val slug: String? = null,
    @SerializedName("translations") val translations: List<LocaleTextResponseOscar>? = null,
)

data class LocaleTextResponseOscar(
    @SerializedName("languageCode") val languageCode: String? = null,
    @SerializedName("languageName") val languageName: String? = null,
    @SerializedName("value") val value: String? = null,
)

data class SegmentResponseOscar(
    @SerializedName("id") val id: Long? = null,
    @SerializedName("legacyId") val legacyId: String? = null,
    @SerializedName("episodeNumber") val episodeNumber: Int? = null,
    @SerializedName("url") val url: String? = null,
    @SerializedName("premium") val premium: Boolean? = null,
    @SerializedName("rating") val rating: BigDecimal? = null,
    @SerializedName("viewCount") val viewCount: Long? = null,
    @SerializedName("thumbnails") val thumbnails: List<LocaleTextResponseOscar>? = null,
    @SerializedName("subtitles") val subtitles: List<LocaleTextResponseOscar>? = null
)