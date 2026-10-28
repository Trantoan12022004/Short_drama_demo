package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


// Quan hệ N-N giữa Movie và Category
@Entity(
    tableName = "movie_category_cross_ref",
    primaryKeys = ["movieId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = MovieEntityOscar::class,
            parentColumns = ["id"],
            childColumns = ["movieId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntityOscar::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId"), Index("movieId")]
)
data class MovieCategoryCrossRefOscar(
    val movieId: Long,
    val categoryId: Long
)