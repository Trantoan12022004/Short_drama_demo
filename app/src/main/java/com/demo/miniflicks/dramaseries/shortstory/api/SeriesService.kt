package com.demo.miniflicks.dramaseries.shortstory.api
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface SeriesService {
    @GET("short-dram/movies")
    suspend fun getAllMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): List<FilmListResponseOscar>

    @GET("short-dram/movies/page")
    suspend fun getAllMoviesByPage(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): FilmPageResponseOscar

    @GET("short-dram/movies/top-viewed")
    suspend fun getTopViewedMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): List<FilmListResponseOscar>

    @GET("short-dram/movies/top-rated")
    suspend fun getTopRatedMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): List<FilmListResponseOscar>

    @GET("short-dram/movies/top-searched")
    suspend fun getTopSearchedMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): List<FilmListResponseOscar>

    @GET("short-dram/movies/by-category")
    suspend fun getMoviesByCategory(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
        @Query("name") name: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): FilmPageResponseOscar

    @GET("short-dram/movies/search")
    suspend fun searchMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
        @Query("name") keyword: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): FilmPageResponseOscar

    @GET("short-dram/categories/names")
    suspend fun getCategoryNames(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): List<String>

    @GET("short-dram/categories/movies/page")
    suspend fun getCategoryMoviesPage(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20
    ): Map<String, FilmPageResponseOscar>

    @GET("short-dram/categories/movies")
    suspend fun getCategoryMovies(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
    ): Map<String, List<FilmListResponseOscar>>

    @GET("short-dram/movies/{id}")
    suspend fun getMovieEpisodeById(
        @Header("X-Api-Key-Token") apiKeyToken: String,
        @Header("X-Package-Name") packageName: String,
        @Header("X-Device-Id") deviceId: String,
        @Header("X-Request-Id") requestId: String,
        @Header("X-Timestamp") timestampMillis: String,
        @Path("id") id: Long
    ): FilmDetailResponseOscar
}