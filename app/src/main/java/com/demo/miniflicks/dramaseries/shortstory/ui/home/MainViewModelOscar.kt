package com.demo.miniflicks.dramaseries.shortstory.ui.home
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.demo.miniflicks.dramaseries.shortstory.api.FilmDetailResponseOscar
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper
import com.demo.miniflicks.dramaseries.shortstory.api.FilmMapper.toMovieCard
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.LocalCatalogRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.LocalEpisodeRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.MovieRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.data.local.repository.SyncRepositoryOscar
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.EpisodeEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.WatchedEpisodeOscar
import com.demo.miniflicks.dramaseries.shortstory.model.toWatchedEpisode
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class MainViewModelOscar(application: Application) : AndroidViewModel(application) {

    private val movieRepositoryOscar = MovieRepositoryOscar(application)
    private val syncRepositoryOscar = SyncRepositoryOscar(application)
    private val localCatalogRepositoryOscar = LocalCatalogRepositoryOscar(application)
    // Repository duy nhất cho toàn bộ trạng thái "đã xem gần đây" + "yêu thích" (thay cho LocalMovieRepository cũ)
    private val localEpisodeRepositoryOscar = LocalEpisodeRepositoryOscar(application)

    private var codeLangOscar = SystemUtilOscar.getPreLanguage(application)

    private val _searchQueryOscar = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQueryOscar

    private val _searchResultsOscar: MutableStateFlow<List<MovieCardOscar>> = MutableStateFlow(emptyList())
    val searchResults: StateFlow<List<MovieCardOscar>> = _searchResultsOscar.asStateFlow()


    private val _isSearchingOscar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearchingOscar.asStateFlow()

    private val _filmDetailOscar: MutableStateFlow<FilmDetailResponseOscar?> = MutableStateFlow(null)
    val filmDetail: StateFlow<FilmDetailResponseOscar?> = _filmDetailOscar.asStateFlow()

    private val _selectedCategoryIdOscar: MutableStateFlow<Long?> = MutableStateFlow(null)


    private val _errorMessageOscar: MutableStateFlow<String?> = MutableStateFlow(null)
    val errorMessage: StateFlow<String?> = _errorMessageOscar.asStateFlow()

    private val _isLoadingOscar: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoadingOscar.asStateFlow()

    // Danh sách tập phim "đã xem gần đây" (chỉ tính tập đã phát > 10 giây, xem PlayVideoActivityOscar).
    // Resolve tên/poster theo codeLang hiện tại khi map sang WatchedEpisodeOscar.
    val recentlyWatched: StateFlow<List<WatchedEpisodeOscar>> = localEpisodeRepositoryOscar.getRecentlyWatchedOscar()
        .map { list -> list.map { entity -> entity.toWatchedEpisode(codeLangOscar) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Danh sách tập phim yêu thích. Resolve tên/poster theo codeLang hiện tại.
    val favorites: StateFlow<List<WatchedEpisodeOscar>> = localEpisodeRepositoryOscar.getFavoriteEpisodesOscar()
        .map { list -> list.map { entity -> entity.toWatchedEpisode(codeLangOscar) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Trending (banner + mini list), cờ yêu thích được suy ra từ trạng thái yêu thích của tập 1.
    // Resolve tên/poster theo codeLang hiện tại khi map sang MovieCardOscar.
    val bannerMovies: StateFlow<List<MovieCardOscar>> = combine(
        localCatalogRepositoryOscar.getTrendingMoviesOscar(),
        localEpisodeRepositoryOscar.getFavoriteEpisodeIdsOscar()
    ) { movieEntities, favoriteEpisodeIds ->
        val favoriteIdSetOscar = favoriteEpisodeIds.toSet()
        movieEntities.map { entity ->
            val isFavoriteOscar = favoriteIdSetOscar.contains(entity.firstEpisodeId)
            entity.toMovieCard(lang = codeLangOscar, isFavorite = isFavoriteOscar)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // New Release. Resolve tên/poster theo codeLang hiện tại.
    val allMovies: StateFlow<List<MovieCardOscar>> = combine(
        localCatalogRepositoryOscar.getNewReleaseMoviesOscar(),
        localEpisodeRepositoryOscar.getFavoriteEpisodeIdsOscar()
    ) { movieEntities, favoriteEpisodeIds ->
        val favoriteIdSetOscar = favoriteEpisodeIds.toSet()
        movieEntities.map { entity ->
            val isFavoriteOscar = favoriteIdSetOscar.contains(entity.firstEpisodeId)
            entity.toMovieCard(lang = codeLangOscar, isFavorite = isFavoriteOscar)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Spotlight. Resolve tên/poster theo codeLang hiện tại.
    val topRateMovies: StateFlow<List<MovieCardOscar>> = combine(
        localCatalogRepositoryOscar.getSpotlightMoviesOscar(),
        localEpisodeRepositoryOscar.getFavoriteEpisodeIdsOscar()
    ) { movieEntities, favoriteEpisodeIds ->
        val favoriteIdSetOscar = favoriteEpisodeIds.toSet()
        movieEntities.map { entity ->
            val isFavoriteOscar = favoriteIdSetOscar.contains(entity.firstEpisodeId)
            entity.toMovieCard(lang = codeLangOscar, isFavorite = isFavoriteOscar)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Top Search. Resolve tên/poster theo codeLang hiện tại.
    val topSearchMovies: StateFlow<List<MovieCardOscar>> = localCatalogRepositoryOscar.getTopSearchMoviesOscar()
        .map { movieEntities -> movieEntities.map { entity -> entity.toMovieCard(lang = codeLangOscar) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    // Phim đại diện cho mục Movie Best (Editor's Choice): ưu tiên phim đầu tiên của topRateMovies, fallback sang bannerMovies
    val bestMovie: StateFlow<MovieCardOscar?> = combine(
        topRateMovies,
        bannerMovies
    ) { topRateList: List<MovieCardOscar>, bannerList: List<MovieCardOscar> ->
        val selectedMovieOscar: MovieCardOscar?
        if (topRateList.isNotEmpty()) {
            selectedMovieOscar = topRateList.first()
        } else if (bannerList.isNotEmpty()) {
            selectedMovieOscar = bannerList.first()
        } else {
            selectedMovieOscar = null
        }
        selectedMovieOscar
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    val allCategories: StateFlow<List<CategoryEntityOscar>> = localCatalogRepositoryOscar.getAllCategoriesOscar()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())
    val movieCategory: StateFlow<List<MovieEntityOscar>> = _selectedCategoryIdOscar
        .flatMapLatest { categoryId ->
            if (categoryId == null) {
                flowOf(emptyList())
            } else {
                localCatalogRepositoryOscar.getMoviesByCategoryIdOscar(categoryId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())



    init {
        syncCatalogOscar()
    }

    fun loadMovieCategory(id: Long) {
        _selectedCategoryIdOscar.value = id
    }

    private fun syncCatalogOscar() {
        viewModelScope.launch {
            _isLoadingOscar.value = true
            val resultOscar = syncRepositoryOscar.syncIfNeededOscar(preferredLangOscar = codeLangOscar)
            if (resultOscar.isFailure) {
                val exceptionOscar = resultOscar.exceptionOrNull()
                _errorMessageOscar.value = "Lỗi đồng bộ dữ liệu: ${exceptionOscar?.message}"
            }
            _isLoadingOscar.value = false
        }
    }

    fun searchMovies(keyword: String) {
        val trimmedKeywordOscar = keyword.trim()
        if (trimmedKeywordOscar.isEmpty()) {
            _searchResultsOscar.value = emptyList()
            _isSearchingOscar.value = false
            return
        }
        viewModelScope.launch {
            _isSearchingOscar.value = true
            val resultOscar = movieRepositoryOscar.searchMoviesOscar(keywordOscar = trimmedKeywordOscar, pageOscar = 0)
            if (resultOscar.isSuccess) {
                val pageResponseOscar = resultOscar.getOrNull()
                if (pageResponseOscar != null) {
                    val filmListOscar = pageResponseOscar.items
                    val movieCardsOscar = filmListOscar.map { filmItem -> filmItem.toMovieCard(preferredLang = codeLangOscar) }
                    _searchResultsOscar.value = movieCardsOscar
                } else {
                    _searchResultsOscar.value = emptyList()
                }
            } else {
                val exceptionOscar = resultOscar.exceptionOrNull()
                val messageOscar = exceptionOscar?.message
                if (messageOscar != null) {
                    _errorMessageOscar.value = messageOscar
                }
                _searchResultsOscar.value = emptyList()
            }
            _isSearchingOscar.value = false
        }
    }

    fun clearSearchResults() {
        _searchResultsOscar.value = emptyList()
        _isSearchingOscar.value = false
    }

    // Toggle yêu thích 1 tập phim cụ thể - dùng cho tab "Yêu thích" trong Playlist
    fun toggleFavorite(episodeId: Long) {
        viewModelScope.launch {
            localEpisodeRepositoryOscar.toggleFavoriteOscar(episodeId)
        }
    }

    // Toggle yêu thích 1 phim (nút tim trên Banner ở Home) - luôn tác động lên tập 1 của phim đó
    fun toggleFavoriteMovie(movie: MovieCardOscar) {
        viewModelScope.launch {
            val movieEntityOscar = localCatalogRepositoryOscar.getMovieByIdOscar(movie.id)
            val firstEpisodeIdOscar = movieEntityOscar?.firstEpisodeId
            if (movieEntityOscar == null || firstEpisodeIdOscar == null) {
                _errorMessageOscar.value = "Không thể cập nhật yêu thích cho phim này"
                return@launch
            }

            val existingEpisodeOscar = localEpisodeRepositoryOscar.getEpisodeByIdOscar(firstEpisodeIdOscar)
            val nextFavoriteStateOscar: Boolean
            if (existingEpisodeOscar != null) {
                nextFavoriteStateOscar = !existingEpisodeOscar.isFavorite
            } else {
                nextFavoriteStateOscar = true
            }

            // Placeholder chỉ dùng nếu tập 1 chưa từng có row trong Room (trường hợp hiếm).
            // Tạo placeholder với toàn bộ raw data đa ngôn ngữ từ MovieEntityOscar.
            val placeholderOscar = EpisodeEntityOscar(
                id = firstEpisodeIdOscar,
                movieId = movieEntityOscar.id,
                movieNames = movieEntityOscar.names,
                posters = movieEntityOscar.covers,
                episodeNumber = 1,
                totalEpisode = movieEntityOscar.totalEpisode,
                url = null,
                premium = false,
                rating = null,
                viewCount = movieEntityOscar.viewCount,
                subtitleUrl = null
            )
            localEpisodeRepositoryOscar.setFavoriteOscar(firstEpisodeIdOscar, nextFavoriteStateOscar, placeholderOscar)
        }
    }

    fun loadFilmDetail(movieId: Long) {
        viewModelScope.launch {
            _isLoadingOscar.value = true

            // Bước 1: Kiểm tra danh sách tập hiện có trong Room DB
            val localEpisodesOscar = localEpisodeRepositoryOscar.getEpisodesForMovieDirectOscar(movieId)
            val hasLocalDataOscar = localEpisodesOscar.isNotEmpty()

            var isFullEpisodesOscar = false
            if (hasLocalDataOscar) {
                val firstEpisodeOscar = localEpisodesOscar[0]
                val expectedTotalOscar = firstEpisodeOscar.totalEpisode
                if (expectedTotalOscar > 0 && localEpisodesOscar.size >= expectedTotalOscar) {
                    isFullEpisodesOscar = true
                }
                val detailOscar = FilmMapper.buildFilmDetailFromEntities(movieId, localEpisodesOscar, codeLangOscar)
                _filmDetailOscar.value = detailOscar
            }

            if (isFullEpisodesOscar) {
                _isLoadingOscar.value = false
                return@launch
            }

            // Bước 2: Gọi API lấy đầy đủ tất cả các tập của phim nếu DB chưa đủ
            val resultOscar = movieRepositoryOscar.getMovieDetailOscar(movieId)
            if (resultOscar.isSuccess) {
                val detailOscar = resultOscar.getOrNull()
                _filmDetailOscar.value = detailOscar
            } else {
                val exceptionOscar = resultOscar.exceptionOrNull()
                Log.e("MainViewModel", "Load film detail failed", exceptionOscar)
                if (!hasLocalDataOscar) {
                    _errorMessageOscar.value = exceptionOscar?.message
                }
            }
            _isLoadingOscar.value = false
        }
    }
}