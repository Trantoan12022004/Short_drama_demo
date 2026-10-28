package com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainViewModelOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.TopSearchMovieCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.PlayVideoActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityMovieTag1Binding
import kotlinx.coroutines.launch
import kotlin.getValue


class TrendingActivityOscar : BaseActivityOscar<ActivityMovieTag1Binding>(ActivityMovieTag1Binding::inflate) {
    // ViewModel quản lý dữ liệu (mỗi Activity tự có 1 instance riêng)
    private val viewModelOscar: MainViewModelOscar by viewModels()

    // Adapter hiển thị danh sách phim theo thể loại (tái dùng layout item_search)
    private val categoryAdapterOscar = TopSearchMovieCardAdapterOscar()

    // Id thể loại được truyền từ màn Home, -1 nghĩa là không hợp lệ

    override fun getData() {
        val categoryNameOscar = intent.getStringExtra("EXTRA_CATEGORY_NAME")
        binding.tvCategoryOscar.text = categoryNameOscar
    }

    override fun initView() {
        // Cài đặt adapter cho RecyclerView
        setupAdaptersOscar()

        // Xử lý sự kiện click nút Back
        setupListenersOscar()

    }

    override fun bindView() {
        // Quan sát dữ liệu từ ViewModel
        observeViewModelOscar()
    }

    private fun setupAdaptersOscar() {
        binding.rvCategoryOscar.adapter = categoryAdapterOscar

        // Xử lý khi click vào 1 phim trong danh sách
        categoryAdapterOscar.onClick = { movieCardOscar ->
            onMovieItemClickedOscar(movieCardOscar)
        }
    }

    private fun setupListenersOscar() {
        // Nút Back quay lại màn hình trước
        binding.btnBackOscar.tap {
            onBack()
        }
    }

    private fun observeViewModelOscar() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                // Quan sát danh sách phim theo thể loại đã chọn
                launch {
                    viewModelOscar.bannerMovies.collect { movieCardsOscar ->
                        categoryAdapterOscar.addListData(movieCardsOscar.toMutableList())
                    }
                }

                // Quan sát trạng thái loading để hiện/ẩn skeleton
                launch {
                    viewModelOscar.isLoading.collect { isLoadingOscar ->
                        categoryAdapterOscar.showSkeleton(isLoadingOscar)
                    }
                }

                // Quan sát thông báo lỗi
                launch {
                    viewModelOscar.errorMessage.collect { messageOscar ->
                        if (messageOscar != null) {
                            Toast.makeText(this@TrendingActivityOscar, messageOscar, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun onMovieItemClickedOscar(movieCardOscar: MovieCardOscar) {
        val bundleOscar = Bundle().apply {
            putLong("EXTRA_MOVIE_ID", movieCardOscar.id)
            putString("EXTRA_MOVIE_TITLE", movieCardOscar.title)
            putString("EXTRA_MOVIE_POSTER", movieCardOscar.posterUrl)
        }
        startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
    }
}