package com.demo.miniflicks.dramaseries.shortstory.ui.home.search
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainViewModelOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.TopSearchMovieCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.PlayVideoActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivitySearchBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SearchActivityOscar : BaseActivityOscar<ActivitySearchBinding>(ActivitySearchBinding::inflate) {

    // ViewModel quản lý dữ liệu
    private val viewModelOscar: MainViewModelOscar by viewModels()

    // Adapter cho danh sách Trending Searches
    private val topSearchAdapterOscar = TopSearchMovieCardAdapterOscar()

    // Adapter cho danh sách Search Results
    private val searchResultAdapterOscar = TopSearchMovieCardAdapterOscar()

    // Job phục vụ debounce khi người dùng gõ tìm kiếm
    private var searchDebounceJobOscar: Job? = null

    // Thời gian debounce (ms)
    private val debounceDelayMsOscar: Long = 400L

    override fun initView() {
        // Cài đặt adapter cho các RecyclerView
        setupAdaptersOscar()

        // Xử lý các sự kiện click và nhập liệu
        setupListenersOscar()
    }

    override fun bindView() {
        // Quan sát dữ liệu từ ViewModel
        observeViewModelOscar()
    }

    /**
     * Khởi tạo và gán adapter cho các RecyclerView
     */
    private fun setupAdaptersOscar() {
        binding.rvTrendingSearchesOscar.adapter = topSearchAdapterOscar
        binding.rvSearchResultsOscar.adapter = searchResultAdapterOscar

        // Xử lý khi click vào phim trong Trending
        topSearchAdapterOscar.onClick = { movieCardOscar: MovieCardOscar ->
            onMovieItemClickedOscar(movieCardOscar)
        }

        // Xử lý khi click vào phim trong Search Results
        searchResultAdapterOscar.onClick = { movieCardOscar: MovieCardOscar ->
            onMovieItemClickedOscar(movieCardOscar)
        }
    }

    /**
     * Cài đặt các listener cho ô tìm kiếm và các nút bấm
     */
    private fun setupListenersOscar() {
        // Nút Back quay lại màn hình trước
        binding.btnBackOscar.tap {
            onBack()
        }

        // Nút Search trên thanh công cụ
        binding.btnSearchOscar.tap {
            val queryOscar = binding.etSearchOscar.text.toString().trim()
            performSearchOscar(queryOscar)
        }

        // Bắt sự kiện nhấn phím Action Search trên bàn phím mềm
        binding.etSearchOscar.setOnEditorActionListener { _, actionIdOscar, _ ->
            if (actionIdOscar == EditorInfo.IME_ACTION_SEARCH) {
                val queryOscar = binding.etSearchOscar.text.toString().trim()
                performSearchOscar(queryOscar)
                true
            } else {
                false
            }
        }

        // Tự động tìm kiếm với debounce khi người dùng gõ từ khóa
        binding.etSearchOscar.doAfterTextChanged { editableOscar ->
            val textOscar = editableOscar?.toString() ?: ""
            val queryOscar = textOscar.trim()

            // Hủy job trước đó nếu người dùng đang tiếp tục gõ
            val currentJobOscar = searchDebounceJobOscar
            if (currentJobOscar != null) {
                currentJobOscar.cancel()
            }

            if (queryOscar.isEmpty()) {
                // Khi xóa hết từ khóa, quay lại hiển thị Trending
                showTrendingLayoutOscar()
                viewModelOscar.clearSearchResults()
            } else {
                // Hiển thị giao diện search kết quả
                showSearchResultsLayoutOscar()

                // Tạo job mới để gọi search sau khoảng thời gian debounce
                searchDebounceJobOscar = lifecycleScope.launch {
                    delay(debounceDelayMsOscar)
                    viewModelOscar.searchMovies(queryOscar)
                }
            }
        }
    }

    /**
     * Thực hiện hành động tìm kiếm ngay lập tức
     */
    private fun performSearchOscar(queryOscar: String) {
        val currentJobOscar = searchDebounceJobOscar
        if (currentJobOscar != null) {
            currentJobOscar.cancel()
        }

        if (queryOscar.isEmpty()) {
            showTrendingLayoutOscar()
            viewModelOscar.clearSearchResults()
        } else {
            showSearchResultsLayoutOscar()
            viewModelOscar.searchMovies(queryOscar)
        }
    }

    /**
     * Cập nhật trạng thái hiển thị của khu vực Search Results
     */
    private fun updateSearchResultUiOscar(isLoadingOscar: Boolean, resultsOscar: List<MovieCardOscar>) {
        // Nếu đang tải dữ liệu từ API
        if (isLoadingOscar) {
            binding.pbSearchLoadingOscar.visible()
            binding.rvSearchResultsOscar.gone()
            binding.layoutEmptyStateOscar.gone()
            return
        }

        // Đã tải xong, ẩn thanh loading
        binding.pbSearchLoadingOscar.gone()

        val currentQueryOscar = binding.etSearchOscar.text.toString().trim()

        // Nếu ô tìm kiếm rỗng, không hiển thị gì ở khu vực search result
        if (currentQueryOscar.isEmpty()) {
            binding.rvSearchResultsOscar.gone()
            binding.layoutEmptyStateOscar.gone()
            return
        }

        // Nếu có từ khóa nhưng không có kết quả trả về -> hiện thông báo No Result
        if (resultsOscar.isEmpty()) {
            binding.rvSearchResultsOscar.gone()
            binding.layoutEmptyStateOscar.visible()
        } else {
            // Có kết quả tìm kiếm -> hiển thị RecyclerView và gán dữ liệu
            binding.layoutEmptyStateOscar.gone()
            binding.rvSearchResultsOscar.visible()
            searchResultAdapterOscar.addListData(resultsOscar.toMutableList())
        }
    }

    /**
     * Quan sát các StateFlow trong MainViewModelOscar
     */
    private fun observeViewModelOscar() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // 1. Quan sát danh sách Top Search Movies (Trending)
                launch {
                    viewModelOscar.topSearchMovies.collect { movieCardsOscar ->
                        if (movieCardsOscar.isNotEmpty()) {
                            topSearchAdapterOscar.addListData(movieCardsOscar.toMutableList())
                        }
                    }
                }

                // 2. Quan sát kết quả tìm kiếm
                launch {
                    viewModelOscar.searchResults.collect { movieCardsOscar ->
                        val currentSearchingOscar = viewModelOscar.isSearching.value
                        updateSearchResultUiOscar(currentSearchingOscar, movieCardsOscar)
                    }
                }

                // 3. Quan sát trạng thái loading khi tìm kiếm
                launch {
                    viewModelOscar.isSearching.collect { isLoadingOscar ->
                        val currentResultsOscar = viewModelOscar.searchResults.value
                        updateSearchResultUiOscar(isLoadingOscar, currentResultsOscar)
                    }
                }

                // 4. Quan sát thông báo lỗi
                launch {
                    viewModelOscar.errorMessage.collect { messageOscar ->
                        if (messageOscar != null) {
                            Toast.makeText(this@SearchActivityOscar, messageOscar, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    /**
     * Chuyển sang hiển thị giao diện Trending Searches
     */
    private fun showTrendingLayoutOscar() {
        binding.layoutTrendingSearchesOscar.visible()
        binding.layoutSearchResultsOscar.gone()
    }

    /**
     * Chuyển sang hiển thị giao diện Search Results
     */
    private fun showSearchResultsLayoutOscar() {
        binding.layoutTrendingSearchesOscar.gone()
        binding.layoutSearchResultsOscar.visible()
    }

    /**
     * Xử lý khi click vào 1 phim
     */
    private fun onMovieItemClickedOscar(movieCardOscar: MovieCardOscar) {
        val bundleOscar = Bundle().apply {
            putLong("EXTRA_MOVIE_ID", movieCardOscar.id)
            putString("EXTRA_MOVIE_TITLE", movieCardOscar.title)
            putString("EXTRA_MOVIE_POSTER", movieCardOscar.posterUrl)
        }
        startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
    }
}