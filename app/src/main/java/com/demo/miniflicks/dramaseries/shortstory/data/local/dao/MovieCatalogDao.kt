package com.demo.miniflicks.dramaseries.shortstory.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCategoryCrossRefOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import kotlinx.coroutines.flow.Flow


@Dao
interface MovieCatalogDao {

    // Đồng bộ toàn bộ catalog trong 1 transaction: xóa dữ liệu cũ rồi chèn dữ liệu mới,
    // tránh trường hợp phim bị mất cờ section (vd hết trending) nhưng row cũ vẫn còn sót lại
    @Transaction
    suspend fun replaceAllOscar(
        categoriesOscar: List<CategoryEntityOscar>,
        moviesOscar: List<MovieEntityOscar>,
        crossRefsOscar: List<MovieCategoryCrossRefOscar>
    ) {
        clearCrossRefsOscar()
        clearMoviesOscar()
        clearCategoriesOscar()
        insertCategoriesOscar(categoriesOscar)
        insertMoviesOscar(moviesOscar)
        insertCrossRefsOscar(crossRefsOscar)
    }

    @Query("DELETE FROM movie_category_cross_ref")
    suspend fun clearCrossRefsOscar()

    @Query("DELETE FROM movies")
    suspend fun clearMoviesOscar()

    @Query("DELETE FROM categories")
    suspend fun clearCategoriesOscar()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoriesOscar(categoriesOscar: List<CategoryEntityOscar>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoviesOscar(moviesOscar: List<MovieEntityOscar>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefsOscar(crossRefsOscar: List<MovieCategoryCrossRefOscar>)

    @Query("SELECT * FROM movies WHERE isTrending = 1 ORDER BY trendingOrder ASC")
    fun getTrendingMoviesOscar(): Flow<List<MovieEntityOscar>>

    @Query("SELECT * FROM movies WHERE isNewRelease = 1 ORDER BY newReleaseOrder ASC")
    fun getNewReleaseMoviesOscar(): Flow<List<MovieEntityOscar>>

    @Query("SELECT * FROM movies WHERE isSpotlight = 1 ORDER BY spotlightOrder ASC")
    fun getSpotlightMoviesOscar(): Flow<List<MovieEntityOscar>>

    @Query("SELECT * FROM movies WHERE isTopSearch = 1")
    fun getTopSearchMoviesOscar(): Flow<List<MovieEntityOscar>>

    @Query("SELECT * FROM movies WHERE id = :movieIdOscar LIMIT 1")
    suspend fun getMovieByIdOscar(movieIdOscar: Long): MovieEntityOscar?

    @Query("SELECT * FROM categories")
    fun getAllCategoriesOscar(): Flow<List<CategoryEntityOscar>>

    @Query("SELECT * FROM movies WHERE id IN (SELECT movieId FROM movie_category_cross_ref WHERE categoryId = :categoryIdOscar)")
    fun getMoviesByCategoryIdOscar(categoryIdOscar: Long): Flow<List<MovieEntityOscar>>
}