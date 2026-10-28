package com.demo.miniflicks.dramaseries.shortstory.ui.home
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.demo.miniflicks.dramaseries.shortstory.base.BaseFragmentOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter.PlaylistAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.PlayVideoActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.FragmentPlaylistBinding
import kotlinx.coroutines.launch



class PlaylistFragmentOscar : BaseFragmentOscar<FragmentPlaylistBinding>() {
    private val viewModelOscar: MainViewModelOscar by activityViewModels()

    // Tab hiện tại: 0 = recently watched, 1 = favorites
    private var currentTabIndexOscar: Int = 0

    // Adapter cho tab "Đã xem gần đây"
    private val recentAdapterOscar = PlaylistAdapterOscar()
    // Adapter cho tab "Yêu thích"
    private val favoriteAdapterOscar = PlaylistAdapterOscar()

    override fun setBinding(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): FragmentPlaylistBinding {
        return FragmentPlaylistBinding.inflate(inflater!!, container, false)
    }

    override fun initView() {
        // Gắn adapter vào RecyclerView
        binding.rvRecentlyOscar.adapter = recentAdapterOscar
        binding.rvFavoriteOscar.adapter = favoriteAdapterOscar

        // Thiết lập listener cho cả 2 adapter
        setupAdapterListenersOscar(recentAdapterOscar)
        setupAdapterListenersOscar(favoriteAdapterOscar)

        // Hiển thị tab mặc định
        updateTabVisibilityOscar()
    }

    override fun bindView() {
        // Bắt đầu observe dữ liệu từ ViewModel
        observeDataOscar()

        // Click tab "Watched"
        binding.tabWatchedOscar.tap {
            if (currentTabIndexOscar != 0) {
                currentTabIndexOscar = 0
                updateTabVisibilityOscar()
            }
        }

        // Click tab "Favorite"
        binding.tabFavoriteOscar.tap {
            if (currentTabIndexOscar != 1) {
                currentTabIndexOscar = 1
                updateTabVisibilityOscar()
            }
        }
    }

    private fun setupAdapterListenersOscar(adapterOscar: PlaylistAdapterOscar) {
        // Click vào item -> mở PlayVideoActivityOscar đúng tập phim
        adapterOscar.onItemClick = { itemOscar ->
            val bundleOscar = Bundle()
            bundleOscar.putLong("EXTRA_MOVIE_ID", itemOscar.movieId)
            bundleOscar.putLong("EXTRA_EPISODE_ID", itemOscar.episodeId)
            bundleOscar.putString("EXTRA_MOVIE_POSTER", itemOscar.posterUrl)
            bundleOscar.putString("EXTRA_MOVIE_TITLE", itemOscar.movieTitle)
            startNextActivity(PlayVideoActivityOscar::class.java, bundleOscar)
        }

        // Click vào nút favorite -> toggle trạng thái yêu thích của tập phim
        adapterOscar.onFavoriteClick = { itemOscar ->
            viewModelOscar.toggleFavorite(itemOscar.episodeId)
        }
    }

    private fun observeDataOscar() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe danh sách recently watched
                launch {
                    viewModelOscar.recentlyWatched.collect { itemListOscar ->
                        recentAdapterOscar.addListData(itemListOscar.toMutableList())
                        // Cập nhật lại giao diện ẩn/hiện empty state khi danh sách thay đổi
                        updateTabVisibilityOscar()
                    }
                }
                // Observe danh sách favorites
                launch {
                    viewModelOscar.favorites.collect { itemListOscar ->
                        favoriteAdapterOscar.addListData(itemListOscar.toMutableList())
                        // Cập nhật lại giao diện ẩn/hiện empty state khi danh sách thay đổi
                        updateTabVisibilityOscar()
                    }
                }
            }
        }
    }

    /**
     * Cập nhật hiển thị giao diện theo tab đang chọn và kiểm tra dữ liệu để ẩn/hiện layoutEmptyState
     */
    private fun updateTabVisibilityOscar() {
        if (currentTabIndexOscar == 0) {
            // --- Tab "Watched" được chọn ---
            // Thiết lập nền có viền phát sáng vàng/cam cho tab Watched
            binding.tabWatchedOscar.setBackgroundResource(R.drawable.bg_watch)
            // Đặt icon Watched có gradient cam-đỏ tự nhiên (history_s)
            binding.ivWatchedOscar.setImageResource(R.drawable.history_s)
            // Thiết lập tab Favorite ở trạng thái unselected (nền trong suốt)
            binding.tabFavoriteOscar.setBackgroundColor(Color.TRANSPARENT)
            // Đặt icon Favorite ở trạng thái unselected (màu xám)
            binding.ivFavoriteOscar.setImageResource(R.drawable.heart_filled_sn)
            // Ẩn danh sách Favorite
            binding.rvFavoriteOscar.gone()
            // Kiểm tra danh sách Watched để ẩn/hiện empty state
            val isRecentEmptyOscar = recentAdapterOscar.itemCount == 0
            if (isRecentEmptyOscar) {
                binding.rvRecentlyOscar.gone()
                binding.layoutEmptyStateOscar.visible()
            } else {
                binding.rvRecentlyOscar.visible()
                binding.layoutEmptyStateOscar.gone()
            }
        } else {
            // --- Tab "Favorite" được chọn ---
            // Thiết lập tab Watched ở trạng thái unselected (nền trong suốt)
            binding.tabWatchedOscar.setBackgroundColor(Color.TRANSPARENT)
            // Đặt icon Watched ở trạng thái unselected (màu xám)
            binding.ivWatchedOscar.setImageResource(R.drawable.history_sn)
            // Thiết lập nền có viền phát sáng vàng/cam cho tab Favorite
            binding.tabFavoriteOscar.setBackgroundResource(R.drawable.bg_watch)
            // Đặt icon Favorite có gradient cam-đỏ tự nhiên (heart_filled_s)
            binding.ivFavoriteOscar.setImageResource(R.drawable.heart_filled_s)
            // Ẩn danh sách Watched
            binding.rvRecentlyOscar.gone()
            // Kiểm tra danh sách Favorite để ẩn/hiện empty state
            val isFavoriteEmptyOscar = favoriteAdapterOscar.itemCount == 0
            if (isFavoriteEmptyOscar) {
                binding.rvFavoriteOscar.gone()
                binding.layoutEmptyStateOscar.visible()
            } else {
                binding.rvFavoriteOscar.visible()
                binding.layoutEmptyStateOscar.gone()
            }
        }
    }
}