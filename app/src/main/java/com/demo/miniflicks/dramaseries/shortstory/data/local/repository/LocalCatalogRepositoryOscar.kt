package com.demo.miniflicks.dramaseries.shortstory.data.local.repository
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.data.local.AppDatabaseOscar
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import kotlinx.coroutines.flow.Flow


class LocalCatalogRepositoryOscar(context: Context) {

    private val movieCatalogDaoOscar = AppDatabaseOscar.getInstanceOscar(context).movieCatalogDaoOscar()

    // Lấy danh sách phim Trending, tự cập nhật khi Room thay đổi
    fun getTrendingMoviesOscar(): Flow<List<MovieEntityOscar>> {
        return movieCatalogDaoOscar.getTrendingMoviesOscar()
    }

    // Lấy danh sách phim New Release
    fun getNewReleaseMoviesOscar(): Flow<List<MovieEntityOscar>> {
        return movieCatalogDaoOscar.getNewReleaseMoviesOscar()
    }

    // Lấy danh sách phim Spotlight
    fun getSpotlightMoviesOscar(): Flow<List<MovieEntityOscar>> {
        return movieCatalogDaoOscar.getSpotlightMoviesOscar()
    }

    // Lấy danh sách phim Top Search
    fun getTopSearchMoviesOscar(): Flow<List<MovieEntityOscar>> {
        return movieCatalogDaoOscar.getTopSearchMoviesOscar()
    }

    // Lấy toàn bộ thể loại
    fun getAllCategoriesOscar(): Flow<List<CategoryEntityOscar>> {
        return movieCatalogDaoOscar.getAllCategoriesOscar()
    }

    // Lấy danh sách phim theo thể loại (dùng cho MovieTagActivityOscar sau này)
    fun getMoviesByCategoryIdOscar(categoryIdOscar: Long): Flow<List<MovieEntityOscar>> {
        return movieCatalogDaoOscar.getMoviesByCategoryIdOscar(categoryIdOscar)
    }

    // Lấy 1 phim theo id từ cache, không gọi API
    suspend fun getMovieByIdOscar(movieIdOscar: Long): MovieEntityOscar? {
        return movieCatalogDaoOscar.getMovieByIdOscar(movieIdOscar)
    }
}