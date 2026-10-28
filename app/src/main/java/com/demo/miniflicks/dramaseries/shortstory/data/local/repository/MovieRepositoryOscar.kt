package com.demo.miniflicks.dramaseries.shortstory.data.local.repository
import android.content.Context
import android.util.Log
import com.demo.miniflicks.dramaseries.shortstory.api.DramaApiIntegrator
import com.demo.miniflicks.dramaseries.shortstory.api.FilmDetailResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.FilmListResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.FilmPageResponseOscar
import com.sulotion.security.KeyStoreManager


class MovieRepositoryOscar(private val contextOscar: Context) {

    // Lấy service singleton và header từ DramaApiIntegrator
    private val serviceOscar = DramaApiIntegrator.getService(contextOscar)
    private fun buildHeadersOscar(): KeyStoreManager.TimeBoundApiHeaders {
        return DramaApiIntegrator.createHeaders(contextOscar)
    }

    // ─── Home Screen ─────────────────────────────────────────

    suspend fun getTopViewedMoviesOscar(): Result<List<FilmListResponseOscar>> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getTopViewedMovies(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun getTopRatedMoviesOscar(): Result<List<FilmListResponseOscar>> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getTopRatedMovies(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun getMoviesByPageOscar(pageOscar: Int, sizeOscar: Int = 20): Result<FilmPageResponseOscar> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getAllMoviesByPage(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp,
                page = pageOscar,
                size = sizeOscar
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun getCategoryNamesOscar(): Result<List<String>> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getCategoryNames(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun getCategoryMoviesOscar(): Result<Map<String, List<FilmListResponseOscar>>> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getCategoryMovies(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    // ─── Search Screen ────────────────────────────────────────

    suspend fun getTopSearchedMoviesOscar(): Result<List<FilmListResponseOscar>> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getTopSearchedMovies(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun searchMoviesOscar(keywordOscar: String, pageOscar: Int = 0, sizeOscar: Int = 20): Result<FilmPageResponseOscar> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.searchMovies(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp,
                keyword = keywordOscar,
                page = pageOscar,
                size = sizeOscar
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    // ─── Movie Detail Screen ──────────────────────────────────

    suspend fun getMovieDetailOscar(movieIdOscar: Long): Result<FilmDetailResponseOscar> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getMovieEpisodeById(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp,
                id = movieIdOscar
            )
            Log.d("DEBUG_PLAYER", "API RESPONSE: $resultOscar")
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }

    suspend fun getMoviesByCategoryOscar(nameOscar: String, pageOscar: Int = 0, sizeOscar: Int = 20): Result<FilmPageResponseOscar> {
        return try {
            val hOscar = buildHeadersOscar()
            val resultOscar = serviceOscar.getMoviesByCategory(
                apiKeyToken = hOscar.apiKeyToken,
                packageName = hOscar.packageName,
                deviceId = hOscar.deviceId,
                requestId = hOscar.requestId,
                timestampMillis = hOscar.timestamp,
                name = nameOscar,
                page = pageOscar,
                size = sizeOscar
            )
            Result.success(resultOscar)
        } catch (eOscar: Exception) {
            Result.failure(eOscar)
        }
    }


}