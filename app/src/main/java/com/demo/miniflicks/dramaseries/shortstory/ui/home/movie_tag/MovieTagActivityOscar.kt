package com.demo.miniflicks.dramaseries.shortstory.ui.home.movie_tag
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.resolvePoster
import com.demo.miniflicks.dramaseries.shortstory.model.resolveTitle
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainViewModelOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.CategoryMovieCardAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.PlayVideoActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityMovieTagBinding
import kotlinx.coroutines.launch


class MovieTagActivityOscar : BaseActivityOscar<ActivityMovieTagBinding>(ActivityMovieTagBinding::inflate) {



    // ViewModel quản lý dữ liệu (mỗi Activity tự có 1 instance riêng)
    private val viewModelOscar: MainViewModelOscar by viewModels()

    // Adapter hiển thị danh sách phim theo thể loại (tái dùng layout item_search)
    private val categoryAdapterOscar = CategoryMovieCardAdapterOscar()

    // Id thể loại được truyền từ màn Home, -1 nghĩa là không hợp lệ
    private var categoryIdOscar: Long = -1L

    override fun getData() {
        categoryIdOscar = intent.getLongExtra("EXTRA_CATEGORY_ID", -1L)
        val categoryNameOscar = intent.getStringExtra("EXTRA_CATEGORY_NAME")
        binding.tvCategoryOscar.text = categoryNameOscar
    }

    override fun initView() {
        // Set ngôn ngữ hiện tại cho adapter trước khi setup
        categoryAdapterOscar.lang = SystemUtilOscar.getPreLanguage(this)

        // Cài đặt adapter cho RecyclerView
        setupAdaptersOscar()

        // Xử lý sự kiện click nút Back
        setupListenersOscar()

        // Yêu cầu ViewModel tải danh sách phim theo categoryId vừa nhận được
        if (categoryIdOscar != -1L) {
            viewModelOscar.loadMovieCategory(categoryIdOscar)
        }
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
                    viewModelOscar.movieCategory.collect { movieCardsOscar ->
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
                            Toast.makeText(this@MovieTagActivityOscar, messageOscar, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun onMovieItemClickedOscar(movieCardOscar: MovieEntityOscar) {
        val langOscar = SystemUtilOscar.getPreLanguage(this)
        val bundleOscar = Bundle().apply {
            putLong("EXTRA_MOVIE_ID", movieCardOscar.id)
            // Resolve tên và poster đúng ngôn ngữ hiện tại
            putString("EXTRA_MOVIE_TITLE", movieCardOscar.resolveTitle(langOscar))
            putString("EXTRA_MOVIE_POSTER", movieCardOscar.resolvePoster(langOscar))
        }
        startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
    }
}