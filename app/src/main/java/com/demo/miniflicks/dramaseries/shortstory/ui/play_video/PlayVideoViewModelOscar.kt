package com.demo.miniflicks.dramaseries.shortstory.ui.play_video
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.demo.miniflicks.dramaseries.shortstory.api.FilmDetailResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.LocalEpisodeRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.MovieRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.WatchedEpisodeOscar
import com.demo.miniflicks.dramaseries.shortstory.model.toWatchedEpisode
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class PlayVideoViewModelOscar(application: Application) : AndroidViewModel(application) {
    private val movieRepositoryOscar = MovieRepositoryOscar(application)
    private val localEpisodeRepositoryOscar = LocalEpisodeRepositoryOscar(application)

    private var codeLangOscar = SystemUtilOscar.getPreLanguage(application)

    // Dữ liệu chi tiết phim phục vụ phát video - 100% được nạp từ Room Database (SSOT)
    private val _filmDetailOscar: MutableStateFlow<FilmDetailResponseOscar?> = MutableStateFlow(null)
    val filmDetail: StateFlow<FilmDetailResponseOscar?> = _filmDetailOscar.asStateFlow()

    private val _currentEpisodeIndexOscar: MutableStateFlow<Int> = MutableStateFlow(0)
    val currentEpisodeIndex: StateFlow<Int> = _currentEpisodeIndexOscar.asStateFlow()

    private val _isLoadingOscar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoadingOscar.asStateFlow()

    private val _errorMessageOscar: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessage: StateFlow<String?> = _errorMessageOscar.asStateFlow()

    // Danh sách tập yêu thích từ Database
    val favorites: StateFlow<List<WatchedEpisodeOscar>> = localEpisodeRepositoryOscar.getFavoriteEpisodesOscar()
        .map { list -> list.map { entity -> entity.toWatchedEpisode(codeLangOscar) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Job theo dõi Flow dữ liệu từ Database cho phim hiện tại
    private var observeDatabaseJobOscar: Job? = null

    // Tải và phát chi tiết phim: 100% dữ liệu hiển thị và phát được lấy từ Database
    fun loadFilmDetail(movieId: Long) {
        // Hủy job theo dõi trước đó nếu có
        val currentJobOscar = observeDatabaseJobOscar
        if (currentJobOscar != null) {
            currentJobOscar.cancel()
        }

        // Bắt đầu quan sát trực tiếp dữ liệu tập phim từ Database
        observeDatabaseJobOscar = viewModelScope.launch {
            localEpisodeRepositoryOscar.getEpisodesForMovieOscar(movieId).collect { episodeEntities ->
                if (episodeEntities.isNotEmpty()) {
                    // Xây dựng FilmDetailResponseOscar trực tiếp từ Room Database
                    val detailFromDbOscar = FilmMapper.buildFilmDetailFromEntities(movieId, episodeEntities, codeLangOscar)
                    _filmDetailOscar.value = detailFromDbOscar

                    // Kiểm tra xem đã đủ toàn bộ các tập của phim chưa
                    val firstEpisodeOscar = episodeEntities[0]
                    val expectedTotalOscar = firstEpisodeOscar.totalEpisode
                    val hasAllEpisodesOscar: Boolean
                    if (expectedTotalOscar > 0 && episodeEntities.size >= expectedTotalOscar) {
                        hasAllEpisodesOscar = true
                    } else {
                        hasAllEpisodesOscar = false
                    }

                    // Nếu đã đủ tập trong Database thì không cần gọi API nữa
                    if (hasAllEpisodesOscar) {
                        _isLoadingOscar.value = false
                        return@collect
                    }
                }

                // Nếu Database chưa có tập nào hoặc chưa đủ tập, gọi API để bổ sung vào Database
                fetchAndCacheMissingEpisodesOscar(movieId, episodeEntities.isEmpty())
            }
        }
    }

    // Gọi API để tải danh sách tập và lưu vào Database (không gán trực tiếp API response vào UI)
    private fun fetchAndCacheMissingEpisodesOscar(movieIdOscar: Long, isDatabaseEmptyOscar: Boolean) {
        viewModelScope.launch {
            if (isDatabaseEmptyOscar) {
                _isLoadingOscar.value = true
            }

            val apiResultOscar = movieRepositoryOscar.getMovieDetailOscar(movieIdOscar)
            if (apiResultOscar.isSuccess) {
                val detailFromApiOscar = apiResultOscar.getOrNull()
                if (detailFromApiOscar != null) {
                    // Lưu dữ liệu vào Room Database. Flow getEpisodesForMovie sẽ tự động phát dữ liệu mới ra UI
                    cacheEpisodesOscar(detailFromApiOscar)
                }
            } else {
                val exceptionOscar = apiResultOscar.exceptionOrNull()
                Log.e("PlayVideoViewModel", "Lỗi tải chi tiết phim từ API", exceptionOscar)
                if (isDatabaseEmptyOscar) {
                    _errorMessageOscar.value = exceptionOscar?.message
                }
            }
            _isLoadingOscar.value = false
        }
    }

    // Cache toàn bộ episode của phim vào Room DB (bảo toàn trạng thái isFavorite và lịch sử xem)
    private suspend fun cacheEpisodesOscar(detailOscar: FilmDetailResponseOscar) {
        val episodeListOscar = detailOscar.episodes
        if (episodeListOscar == null) {
            return
        }
        val episodeEntitiesOscar = mutableListOf<EpisodeEntityOscar>()
        for (segmentOscar in episodeListOscar) {
            val entityOscar = FilmMapper.toEpisodeEntity(detailOscar, segmentOscar)
            if (entityOscar != null) {
                episodeEntitiesOscar.add(entityOscar)
            }
        }
        localEpisodeRepositoryOscar.upsertEpisodesMetadataOscar(episodeEntitiesOscar)
    }

    fun setCurrentEpisodeIndex(index: Int) {
        _currentEpisodeIndexOscar.value = index
    }

    // Toggle yêu thích theo episodeId
    fun toggleFavorite(episodeId: Long) {
        viewModelScope.launch {
            localEpisodeRepositoryOscar.toggleFavoriteOscar(episodeId)
        }
    }

    // Đánh dấu 1 tập là "đã xem gần đây" kèm tiến trình xem
    fun updateWatchProgress(episodeId: Long, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            localEpisodeRepositoryOscar.updateWatchProgressOscar(episodeId, positionMs, durationMs)
        }
    }

    // Đánh dấu 1 tập là "đã xem gần đây" (ngưỡng 10 giây)
    fun markEpisodeAsRecentlyWatched(episodeId: Long) {
        viewModelScope.launch {
            localEpisodeRepositoryOscar.markAsRecentlyWatchedOscar(episodeId)
        }
    }
}