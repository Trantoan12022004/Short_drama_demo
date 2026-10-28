package com.demo.miniflicks.dramaseries.shortstory.model
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation


// Dùng khi cần đọc 1 phim kèm toàn bộ thể loại của nó trong 1 lần query
data class MovieWithCategoriesOscar(
    @Embedded val movie: MovieEntityOscar,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieCategoryCrossRefOscar::class,
            parentColumn = "movieId",
            entityColumn = "categoryId"
        )
    )
    val categories: List<CategoryEntityOscar>
)

// Dùng khi cần đọc 1 phim kèm các tập đã cache của nó
data class MovieWithEpisodesOscar(
    @Embedded val movie: MovieEntityOscar,
    @Relation(parentColumn = "id", entityColumn = "movieId")
    val episodes: List<EpisodeEntityOscar>
)