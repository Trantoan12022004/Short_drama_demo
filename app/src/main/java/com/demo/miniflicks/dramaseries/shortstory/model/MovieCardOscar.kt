package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.PrimaryKey


data class MovieCardOscar(
    @PrimaryKey val id: Long,
    val title: String,
    val posterUrl: String,
    val rating: Float,
    val viewCount: Long,
    val badgeText: String?,
    val watchTimeMs: Long = 0L,
    var isFavorite: Boolean = false,
)