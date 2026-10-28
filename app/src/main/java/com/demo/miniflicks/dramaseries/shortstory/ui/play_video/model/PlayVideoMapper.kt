package com.demo.miniflicks.dramaseries.shortstory.ui.play_video.model
import com.demo.miniflicks.dramaseries.shortstory.api.FilmDetailResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.SegmentResponseOscar


object PlayVideoMapper {

    fun buildEpisodeNumberList(detail: FilmDetailResponseOscar): List<EpisodeNumberItemOscar> {
        val totalEpisodeOscar: Int
        if (detail.totalEpisode != null) {
            totalEpisodeOscar = detail.totalEpisode
        } else {
            val episodeListSizeOscar = detail.episodes?.size
            if (episodeListSizeOscar != null) {
                totalEpisodeOscar = episodeListSizeOscar
            } else {
                totalEpisodeOscar = 0
            }
        }

        // Map episodeNumber -> SegmentResponseOscar để tra cứu nhanh
        val segmentByNumberOscar = mutableMapOf<Int, SegmentResponseOscar>()
        val episodeListOscar = detail.episodes
        if (episodeListOscar != null) {
            for (segment in episodeListOscar) {
                val episodeNumberOscar = segment.episodeNumber
                if (episodeNumberOscar != null) {
                    segmentByNumberOscar[episodeNumberOscar] = segment
                }
            }
        }

        val resultOscar = mutableListOf<EpisodeNumberItemOscar>()
        var indexOscar = 0
        while (indexOscar < totalEpisodeOscar) {
            val segmentOscar = segmentByNumberOscar[indexOscar]
            val hasUrlOscar: Boolean
            if (segmentOscar != null && !segmentOscar.url.isNullOrBlank()) {
                hasUrlOscar = true
            } else {
                hasUrlOscar = false
            }
            resultOscar.add(EpisodeNumberItemOscar(number = indexOscar, hasVideoUrl = hasUrlOscar))
            indexOscar = indexOscar + 1
        }
        return resultOscar
    }
}