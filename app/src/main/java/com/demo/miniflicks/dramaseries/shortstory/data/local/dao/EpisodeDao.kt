package com.demo.miniflicks.dramaseries.shortstory.data.local.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import kotlinx.coroutines.flow.Flow


@Dao
interface EpisodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodeOscar(episodeOscar: EpisodeEntityOscar)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisodesOscar(episodesOscar: List<EpisodeEntityOscar>)

    @Query("SELECT * FROM episodes WHERE id = :episodeIdOscar LIMIT 1")
    suspend fun getEpisodeByIdOscar(episodeIdOscar: Long): EpisodeEntityOscar?

    @Query("SELECT * FROM episodes WHERE movieId = :movieIdOscar ORDER BY episodeNumber ASC")
    fun getEpisodesForMovieOscar(movieIdOscar: Long): Flow<List<EpisodeEntityOscar>>

    // Lấy toàn bộ danh sách tập của phim trực tiếp dưới dạng List
    @Query("SELECT * FROM episodes WHERE movieId = :movieIdOscar ORDER BY episodeNumber ASC")
    suspend fun getEpisodesForMovieDirectOscar(movieIdOscar: Long): List<EpisodeEntityOscar>

    // Chỉ lấy các tập đã phát vượt ngưỡng "đã xem" (lastWatchedAt > 0)
    @Query("SELECT * FROM episodes WHERE lastWatchedAt > 0 ORDER BY lastWatchedAt DESC")
    fun getRecentlyWatchedOscar(): Flow<List<EpisodeEntityOscar>>

    // Lấy danh sách tập yêu thích: sắp xếp theo thời điểm thêm yêu thích mới nhất lên đầu
    @Query("SELECT * FROM episodes WHERE isFavorite = 1 ORDER BY favoritedAt DESC, lastWatchedAt DESC, cachedAt DESC")
    fun getFavoriteEpisodesOscar(): Flow<List<EpisodeEntityOscar>>

    // Danh sách id các tập đang được yêu thích - dùng để merge cờ isFavorite vào MovieCardOscar ở màn Home
    @Query("SELECT id FROM episodes WHERE isFavorite = 1")
    fun getFavoriteEpisodeIdsOscar(): Flow<List<Long>>

    // Cập nhật cả cờ yêu thích và mốc thời gian yêu thích
    @Query("UPDATE episodes SET isFavorite = :isFavoriteOscar, favoritedAt = :favoritedAtOscar WHERE id = :episodeIdOscar")
    suspend fun updateFavoriteOscar(episodeIdOscar: Long, isFavoriteOscar: Boolean, favoritedAtOscar: Long)

    @Query("UPDATE episodes SET lastWatchedAt = :timestampOscar WHERE id = :episodeIdOscar")
    suspend fun updateLastWatchedAtOscar(episodeIdOscar: Long, timestampOscar: Long)

    @Query("UPDATE episodes SET lastWatchedAt = :timestampOscar, lastPositionMs = :positionMsOscar, durationMs = :durationMsOscar WHERE id = :episodeIdOscar")
    suspend fun updateWatchProgressOscar(episodeIdOscar: Long, timestampOscar: Long, positionMsOscar: Long, durationMsOscar: Long)

    // Lưu hàng loạt danh sách tập phim từ API, tự động bảo toàn trạng thái yêu thích và lịch sử xem của người dùng
    @Transaction
    suspend fun upsertEpisodesPreservingUserDataOscar(newEpisodesOscar: List<EpisodeEntityOscar>) {
        val mergedListOscar = mutableListOf<EpisodeEntityOscar>()
        for (itemOscar in newEpisodesOscar) {
            val existingOscar = getEpisodeByIdOscar(itemOscar.id)
            val mergedOscar: EpisodeEntityOscar
            if (existingOscar != null) {
                // Giữ lại toàn bộ dữ liệu cá nhân của người dùng đã có trước đó
                mergedOscar = itemOscar.copy(
                    isFavorite = existingOscar.isFavorite,
                    favoritedAt = existingOscar.favoritedAt,
                    lastWatchedAt = existingOscar.lastWatchedAt,
                    lastPositionMs = existingOscar.lastPositionMs,
                    durationMs = existingOscar.durationMs
                )
            } else {
                mergedOscar = itemOscar
            }
            mergedListOscar.add(mergedOscar)
        }
        insertEpisodesOscar(mergedListOscar)
    }
}