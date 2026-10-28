package com.demo.miniflicks.dramaseries.shortstory.data.local.repository
import android.content.Context
import android.util.Log
import com.demo.miniflicks.dramaseries.shortstory.api.FilmListResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper.toCategoryEntity
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper.toFirstEpisodeEntity
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper.toMovieEntity
import com.demo.miniflicks.dramaseries.shortstory.data.local.AppDatabaseOscar
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCategoryCrossRefOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class SyncRepositoryOscar(private val contextOscar: Context) {

    companion object {
        private const val TAGOscar = "SyncRepository"
        // Key lưu ngày đồng bộ thành công gần nhất định dạng yyyy-MM-dd
        private const val KEY_LAST_SYNC_DATEOscar = "KEY_CATALOG_LAST_SYNC_DATE"
    }

    private val remoteRepositoryOscar = MovieRepositoryOscar(contextOscar)
    private val movieCatalogDaoOscar = AppDatabaseOscar.getInstanceOscar(contextOscar).movieCatalogDaoOscar()
    private val localEpisodeRepositoryOscar = LocalEpisodeRepositoryOscar(contextOscar)

    // Lấy chuỗi ngày hiện tại theo lịch máy
    private fun getTodayDateStringOscar(): String {
        val dateFormatOscar = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentDateOscar = Date()
        val formattedDateOscar = dateFormatOscar.format(currentDateOscar)
        return formattedDateOscar
    }

    // Kiểm tra xem đã qua ngày mới chưa. Nếu sang ngày mới hoặc forceSync thì thực hiện đồng bộ lại toàn bộ
    suspend fun syncIfNeededOscar(preferredLangOscar: String = "en", forceSyncOscar: Boolean = false): Result<Unit> {
        val prefsOscar = SharedPreUtilsOscar.getInstance()
        val lastSyncDateOscar = prefsOscar.getString(KEY_LAST_SYNC_DATEOscar, "")
        val todayDateOscar = getTodayDateStringOscar()

        val isDifferentDayOscar = lastSyncDateOscar != todayDateOscar

        // Nếu cùng ngày và không yêu cầu ép đồng bộ thì bỏ qua
        if (!forceSyncOscar && !isDifferentDayOscar) {
            Log.d(TAGOscar, "Dữ liệu hôm nay ($todayDateOscar) đã được đồng bộ, bỏ qua.")
            return Result.success(Unit)
        }

        Log.d(TAGOscar, "Bắt đầu làm mới dữ liệu từ API cho ngày: $todayDateOscar (lần trước: $lastSyncDateOscar)")
        val syncResultOscar = syncAllDataOscar()
        if (syncResultOscar.isSuccess) {
            prefsOscar.setString(KEY_LAST_SYNC_DATEOscar, todayDateOscar)
            Log.d(TAGOscar, "Đồng bộ thành công và đã lưu mốc ngày: $todayDateOscar")
        }
        return syncResultOscar
    }

    // Tải toàn bộ Catalog và chi tiết các tập của tất cả phim lưu vào Room DB
    suspend fun syncAllDataOscar(): Result<Unit> = coroutineScope {
        try {
            // Bước 1: Tải đồng thời 4 danh mục phim từ API để tiết kiệm thời gian
            val trendingDeferredOscar = async { remoteRepositoryOscar.getTopViewedMoviesOscar() }
            val spotlightDeferredOscar = async { remoteRepositoryOscar.getTopRatedMoviesOscar() }
            val newReleaseDeferredOscar = async { remoteRepositoryOscar.getMoviesByPageOscar(pageOscar = 0) }
            val topSearchDeferredOscar = async { remoteRepositoryOscar.getTopSearchedMoviesOscar() }

            val trendingResultOscar = trendingDeferredOscar.await()
            val spotlightResultOscar = spotlightDeferredOscar.await()
            val newReleaseResultOscar = newReleaseDeferredOscar.await()
            val topSearchResultOscar = topSearchDeferredOscar.await()

            val trendingListOscar = trendingResultOscar.getOrDefault(emptyList())
            val spotlightListOscar = spotlightResultOscar.getOrDefault(emptyList())

            val newReleaseListOscar: List<FilmListResponseOscar>
            val newReleasePageOscar = newReleaseResultOscar.getOrNull()
            if (newReleasePageOscar != null) {
                newReleaseListOscar = newReleasePageOscar.items
            } else {
                newReleaseListOscar = emptyList()
            }

            val topSearchListOscar = topSearchResultOscar.getOrDefault(emptyList())

            val mergedMoviesOscar: MutableMap<Long, MovieEntityOscar> = mutableMapOf()
            val allCategoriesOscar: MutableMap<Long, CategoryEntityOscar> = mutableMapOf()
            val crossRefSetOscar: MutableSet<MovieCategoryCrossRefOscar> = mutableSetOf()

            // Merge từng section vào map tổng
            mergeSectionOscar(trendingListOscar, mergedMoviesOscar, allCategoriesOscar, crossRefSetOscar) { entityOscar, orderOscar ->
                entityOscar.copy(isTrending = true, trendingOrder = orderOscar)
            }
            mergeSectionOscar(spotlightListOscar, mergedMoviesOscar, allCategoriesOscar, crossRefSetOscar) { entityOscar, orderOscar ->
                entityOscar.copy(isSpotlight = true, spotlightOrder = orderOscar)
            }
            mergeSectionOscar(newReleaseListOscar, mergedMoviesOscar, allCategoriesOscar, crossRefSetOscar) { entityOscar, orderOscar ->
                entityOscar.copy(isNewRelease = true, newReleaseOrder = orderOscar)
            }
            mergeSectionOscar(topSearchListOscar, mergedMoviesOscar, allCategoriesOscar, crossRefSetOscar) { entityOscar, _ ->
                entityOscar.copy(isTopSearch = true)
            }

            // Ghi đè toàn bộ catalog ngày mới vào Room Database (Reset dữ liệu catalog theo ngày)
            movieCatalogDaoOscar.replaceAllOscar(
                categoriesOscar = allCategoriesOscar.values.toList(),
                moviesOscar = mergedMoviesOscar.values.toList(),
                crossRefsOscar = crossRefSetOscar.toList()
            )

            // Bước 2: Lưu ngay episode 1 cho các phim để có dữ liệu sơ bộ
            val allFilmsOscar = trendingListOscar + spotlightListOscar + newReleaseListOscar + topSearchListOscar
            val firstEpisodeByIdOscar: MutableMap<Long, EpisodeEntityOscar> = mutableMapOf()
            for (filmItemOscar in allFilmsOscar) {
                val episodeEntityOscar = filmItemOscar.toFirstEpisodeEntity()
                if (episodeEntityOscar != null) {
                    firstEpisodeByIdOscar[episodeEntityOscar.id] = episodeEntityOscar
                }
            }
            if (firstEpisodeByIdOscar.isNotEmpty()) {
                localEpisodeRepositoryOscar.upsertEpisodesMetadataOscar(firstEpisodeByIdOscar.values.toList())
            }

            // Bước 3: Tải chi tiết toàn bộ các tập của tất cả các phim song song theo chunk 5 phim
            val uniqueMovieIdsOscar = mergedMoviesOscar.keys.toList()
            val chunkedMovieIdsOscar = uniqueMovieIdsOscar.chunked(5)
            val allEpisodeEntitiesOscar = mutableListOf<EpisodeEntityOscar>()

            for (chunkOscar in chunkedMovieIdsOscar) {
                val deferredEpisodeTasksOscar = chunkOscar.map { movieIdOscar ->
                    async {
                        val movieDetailResultOscar = remoteRepositoryOscar.getMovieDetailOscar(movieIdOscar)
                        val movieDetailOscar = movieDetailResultOscar.getOrNull()
                        val episodeListOscar = movieDetailOscar?.episodes
                        val extractedEntitiesOscar = mutableListOf<EpisodeEntityOscar>()
                        if (movieDetailOscar != null && episodeListOscar != null) {
                            for (segmentOscar in episodeListOscar) {
                                val entityOscar = FilmMapper.toEpisodeEntity(movieDetailOscar, segmentOscar)
                                if (entityOscar != null) {
                                    extractedEntitiesOscar.add(entityOscar)
                                }
                            }
                        }
                        extractedEntitiesOscar
                    }
                }
                val batchResultsOscar = deferredEpisodeTasksOscar.awaitAll()
                for (batchOscar in batchResultsOscar) {
                    allEpisodeEntitiesOscar.addAll(batchOscar)
                }
            }

            // Lưu toàn bộ danh sách tập vào Room DB, tự động bảo toàn trạng thái yêu thích và lịch sử xem của user
            if (allEpisodeEntitiesOscar.isNotEmpty()) {
                localEpisodeRepositoryOscar.upsertEpisodesMetadataOscar(allEpisodeEntitiesOscar)
            }

            Result.success(Unit)
        } catch (eOscar: Exception) {
            Log.e(TAGOscar, "Đồng bộ dữ liệu ngày mới thất bại", eOscar)
            Result.failure(eOscar)
        }
    }

    // Merge 1 danh sách phim vào map tổng
    private fun mergeSectionOscar(
        sourceOscar: List<FilmListResponseOscar>,
        mergedMoviesOscar: MutableMap<Long, MovieEntityOscar>,
        allCategoriesOscar: MutableMap<Long, CategoryEntityOscar>,
        crossRefSetOscar: MutableSet<MovieCategoryCrossRefOscar>,
        applyFlagOscar: (MovieEntityOscar, Int) -> MovieEntityOscar
    ) {
        var indexOscar = 0
        for (filmItemOscar in sourceOscar) {
            val existingEntityOscar = mergedMoviesOscar[filmItemOscar.id]
            val baseEntityOscar: MovieEntityOscar
            if (existingEntityOscar != null) {
                baseEntityOscar = existingEntityOscar
            } else {
                baseEntityOscar = filmItemOscar.toMovieEntity()
            }

            val updatedEntityOscar = applyFlagOscar(baseEntityOscar, indexOscar)
            mergedMoviesOscar[filmItemOscar.id] = updatedEntityOscar

            for (genreOscar in filmItemOscar.categories) {
                val categoryEntityOscar = genreOscar.toCategoryEntity()
                if (categoryEntityOscar != null) {
                    allCategoriesOscar[categoryEntityOscar.id] = categoryEntityOscar
                    crossRefSetOscar.add(
                        MovieCategoryCrossRefOscar(
                            movieId = filmItemOscar.id,
                            categoryId = categoryEntityOscar.id
                        )
                    )
                }
            }
            indexOscar = indexOscar + 1
        }
    }
}