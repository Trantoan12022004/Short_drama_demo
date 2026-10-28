package com.demo.miniflicks.dramaseries.shortstory.data.local.repository
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.data.local.AppDatabaseOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import kotlinx.coroutines.flow.Flow


class LocalEpisodeRepositoryOscar(context: Context) {

    private val episodeDaoOscar = AppDatabaseOscar.getInstanceOscar(context).episodeDaoOscar()

    fun getRecentlyWatchedOscar(): Flow<List<EpisodeEntityOscar>> {
        return episodeDaoOscar.getRecentlyWatchedOscar()
    }

    fun getFavoriteEpisodesOscar(): Flow<List<EpisodeEntityOscar>> {
        return episodeDaoOscar.getFavoriteEpisodesOscar()
    }

    fun getFavoriteEpisodeIdsOscar(): Flow<List<Long>> {
        return episodeDaoOscar.getFavoriteEpisodeIdsOscar()
    }

    fun getEpisodesForMovieOscar(movieIdOscar: Long): Flow<List<EpisodeEntityOscar>> {
        return episodeDaoOscar.getEpisodesForMovieOscar(movieIdOscar)
    }

    suspend fun getEpisodesForMovieDirectOscar(movieIdOscar: Long): List<EpisodeEntityOscar> {
        return episodeDaoOscar.getEpisodesForMovieDirectOscar(movieIdOscar)
    }

    suspend fun getEpisodeByIdOscar(episodeIdOscar: Long): EpisodeEntityOscar? {
        return episodeDaoOscar.getEpisodeByIdOscar(episodeIdOscar)
    }

    // Lưu/cập nhật metadata 1 tập lấy từ API, LUÔN giữ nguyên isFavorite/favoritedAt/lastWatchedAt cũ nếu tập đã
    // tồn tại, để việc đồng bộ lại catalog hoặc mở lại chi tiết phim không làm mất trạng thái người dùng
    suspend fun upsertEpisodeMetadataOscar(newDataOscar: EpisodeEntityOscar) {
        val existingOscar = episodeDaoOscar.getEpisodeByIdOscar(newDataOscar.id)
        val mergedOscar: EpisodeEntityOscar
        if (existingOscar != null) {
            mergedOscar = newDataOscar.copy(
                isFavorite = existingOscar.isFavorite,
                favoritedAt = existingOscar.favoritedAt,
                lastWatchedAt = existingOscar.lastWatchedAt
            )
        } else {
            mergedOscar = newDataOscar
        }
        episodeDaoOscar.insertEpisodeOscar(mergedOscar)
    }

    suspend fun upsertEpisodesMetadataOscar(newDataListOscar: List<EpisodeEntityOscar>) {
        if (newDataListOscar.isEmpty()) {
            return
        }
        episodeDaoOscar.upsertEpisodesPreservingUserDataOscar(newDataListOscar)
    }

    // Đánh dấu 1 tập là "đã xem gần đây" - chỉ nên gọi khi đã phát vượt ngưỡng thời gian quy định (10s)
    suspend fun markAsRecentlyWatchedOscar(episodeIdOscar: Long) {
        val currentTimeOscar = System.currentTimeMillis()
        episodeDaoOscar.updateLastWatchedAtOscar(episodeIdOscar, currentTimeOscar)
    }

    // Cập nhật tiến trình xem cụ thể (vị trí phát dở và tổng thời lượng)
    suspend fun updateWatchProgressOscar(episodeIdOscar: Long, positionMsOscar: Long, durationMsOscar: Long) {
        val currentTimeOscar = System.currentTimeMillis()
        episodeDaoOscar.updateWatchProgressOscar(episodeIdOscar, currentTimeOscar, positionMsOscar, durationMsOscar)
    }

    // Toggle yêu thích theo episodeId - dùng cho tab "Yêu thích" trong Playlist và Player
    suspend fun toggleFavoriteOscar(episodeIdOscar: Long) {
        val existingOscar = episodeDaoOscar.getEpisodeByIdOscar(episodeIdOscar)
        if (existingOscar != null) {
            val nextFavoriteOscar = !existingOscar.isFavorite
            val favoritedAtOscar: Long
            if (nextFavoriteOscar) {
                favoritedAtOscar = System.currentTimeMillis()
            } else {
                favoritedAtOscar = 0L
            }
            episodeDaoOscar.updateFavoriteOscar(episodeIdOscar, nextFavoriteOscar, favoritedAtOscar)
        }
    }

    // Set thẳng trạng thái yêu thích cho 1 tập. Nếu tập chưa từng được cache (vd yêu thích 1 phim
    // ngay từ Home trước khi kịp đồng bộ xong), tự tạo 1 row mới từ placeholder được truyền vào.
    suspend fun setFavoriteOscar(episodeIdOscar: Long, isFavoriteOscar: Boolean, placeholderIfMissingOscar: EpisodeEntityOscar) {
        val existingOscar = episodeDaoOscar.getEpisodeByIdOscar(episodeIdOscar)
        val favoritedAtOscar: Long
        if (isFavoriteOscar) {
            favoritedAtOscar = System.currentTimeMillis()
        } else {
            favoritedAtOscar = 0L
        }
        if (existingOscar != null) {
            episodeDaoOscar.updateFavoriteOscar(episodeIdOscar, isFavoriteOscar, favoritedAtOscar)
        } else {
            episodeDaoOscar.insertEpisodeOscar(
                placeholderIfMissingOscar.copy(
                    isFavorite = isFavoriteOscar,
                    favoritedAt = favoritedAtOscar
                )
            )
        }
    }
}