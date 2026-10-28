package com.demo.miniflicks.dramaseries.shortstory.ui.play_video.model

// number: số thứ tự tập hiển thị (bắt đầu từ 0, cộng 1 khi hiển thị UI)
// hasVideoUrl: false khi tập chưa có url video -> hiện khóa
data class EpisodeNumberItemOscar(
    val number: Int,
    val hasVideoUrl: Boolean
)