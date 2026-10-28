package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTrendingExpandBinding
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTrendingExpandSkeletonBinding
import java.util.Locale


class BannerAdapterOscar : BaseAdapterOscar<MovieCardOscar, ViewBinding>() {

    var onItemClick: ((MovieCardOscar) -> Unit)? = null
    var onWatchNowClick: ((MovieCardOscar) -> Unit)? = null
    var onFavoriteClick: ((MovieCardOscar) -> Unit)? = null

    // Cờ loading: true = hiện skeleton, false = hiện data thật
    private var isLoadingOscar: Boolean = true

    // Số lượng skeleton item khi đang load
    private val skeletonCountOscar: Int = 3

    companion object {
        const val VIEW_TYPE_SKELETONOscar = 0
        const val VIEW_TYPE_DATAOscar = 1
        const val PAYLOAD_FAVORITEOscar = "PAYLOAD_FAVORITE"
    }

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        val inflatedBindingOscar: ViewBinding
        if (viewType == VIEW_TYPE_SKELETONOscar) {
            inflatedBindingOscar = ItemTrendingExpandSkeletonBinding.inflate(inflater, parent, false)
        } else {
            inflatedBindingOscar = ItemTrendingExpandBinding.inflate(inflater, parent, false)
        }
        return inflatedBindingOscar
    }

    override fun addListData(newList: MutableList<MovieCardOscar>) {
        // Nếu danh sách không rỗng và nội dung không thay đổi, bỏ qua không re-bind
        if (!isLoadingOscar && listData.size == newList.size) {
            var hasDifferenceOscar = false
            for (index in 0 until listData.size) {
                val currentMovieOscar: MovieCardOscar = listData[index]
                val newMovieOscar: MovieCardOscar = newList[index]
                if (currentMovieOscar != newMovieOscar) {
                    hasDifferenceOscar = true
                    break
                }
            }
            if (!hasDifferenceOscar) {
                return
            }
        }

        // Cập nhật danh sách dữ liệu mới
        listData.clear()
        listData.addAll(newList)

        // Tắt trạng thái skeleton nếu đang bật
        if (isLoadingOscar) {
            isLoadingOscar = false
        }

        // Cập nhật lại toàn bộ giao diện adapter
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        val viewTypeOscar: Int
        if (isLoadingOscar) {
            viewTypeOscar = VIEW_TYPE_SKELETONOscar
        } else {
            viewTypeOscar = VIEW_TYPE_DATAOscar
        }
        return viewTypeOscar
    }

    override fun getItemCount(): Int {
        val itemCountOscar: Int
        if (isLoadingOscar) {
            itemCountOscar = skeletonCountOscar
        } else if (listData.isNotEmpty()) {
            // Infinite scroll cho ViewPager2
            itemCountOscar = Int.MAX_VALUE
        } else {
            itemCountOscar = 0
        }
        return itemCountOscar
    }

    override fun onBindViewHolder(
        holder: ViewHolderOscar,
        position: Int,
        payloads: MutableList<Any>
    ) {
        // Nếu có payload yêu cầu cập nhật favorite và đang không ở chế độ skeleton
        if (payloads.isNotEmpty() && !isLoadingOscar && listData.isNotEmpty()) {
            val listSizeOscar = listData.size
            val actualPositionOscar = position % listSizeOscar
            val itemOscar = listData[actualPositionOscar]
            val bindingOscar = holder.binding
            if (bindingOscar is ItemTrendingExpandBinding) {
                // Chỉ cập nhật lại icon favorite mà không reload poster Glide
                updateFavoriteIconOscar(bindingOscar.btnFavoriteOscar, itemOscar.isFavorite)
                return
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onBindViewHolder(holder: ViewHolderOscar, position: Int) {
        val viewTypeOscar = getItemViewType(position)
        if (viewTypeOscar == VIEW_TYPE_SKELETONOscar) {
            startSkeletonAnimationOscar(holder.binding)
        } else {
            val listSizeOscar = listData.size
            if (listSizeOscar == 0) {
                return
            }
            // Tính vị trí thực trong list (infinite scroll)
            val actualPositionOscar = position % listSizeOscar
            val itemOscar = listData[actualPositionOscar]
            holder.bindData(itemOscar)
        }
    }

    override fun setData(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        if (binding !is ItemTrendingExpandBinding) {
            return
        }

        val currentContextOscar = binding.root.context

        // Load ảnh poster kèm ProgressBar xoay tròn loading độc lập
        binding.ivCardMovieOscar.loadImageWithProgress(
            url = item.posterUrl,
            progressBar = binding.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )

        // Bind rating
        val ratingTextOscar = String.format(Locale.US, "%.1f", item.rating)
        binding.tvRatingOscar.text = ratingTextOscar

        // Bind icon yêu thích
        updateFavoriteIconOscar(binding.btnFavoriteOscar, item.isFavorite)
    }

    override fun onCLick(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        if (binding !is ItemTrendingExpandBinding) {
            return
        }

//        binding.btnWatchNow.setOnClickListener {
//            val listener = onWatchNowClick
//            if (listener != null) {
//                listener(item)
//            }
//        }

        // Click Favorite: Optimistic UI update + bounce animation ngay lập tức
        binding.btnFavoriteOscar.setOnClickListener {
            // Đảo trạng thái trong model item
            val nextFavoriteStateOscar = !item.isFavorite
            item.isFavorite = nextFavoriteStateOscar
            // Cập nhật icon ngay tức thì
            updateFavoriteIconOscar(binding.btnFavoriteOscar, nextFavoriteStateOscar)
            // Hiệu ứng nảy mượt mà
            animateBounceOscar(binding.btnFavoriteOscar)
            // Gửi callback cập nhật DB
            val listenerOscar = onFavoriteClick
            if (listenerOscar != null) {
                listenerOscar(item)
            }
        }

        binding.root.setOnClickListener {
            val listenerOscar = onItemClick
            if (listenerOscar != null) {
                listenerOscar(item)
            }
        }
    }

    private fun updateFavoriteIconOscar(viewOscar: ImageView, isFavoriteOscar: Boolean) {
        val favoriteIconOscar: Int
        if (isFavoriteOscar) {
            favoriteIconOscar = R.drawable.boxicons_heart_filled_sn
        } else {
            favoriteIconOscar = R.drawable.boxicons_heart_filled
        }
        viewOscar.setImageResource(favoriteIconOscar)
    }

    private fun animateBounceOscar(viewOscar: View) {
        viewOscar.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100L)
            .withEndAction {
                viewOscar.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .setDuration(100L)
                    .start()
            }
            .start()
    }

    fun showSkeleton(show: Boolean) {
        if (show) {
            // Nếu adapter đã có dữ liệu thật từ trước thì KHÔNG quay lại skeleton
            // để tránh banner bị giật/loading khi background sync hoặc khi quay lại màn hình
            if (listData.isNotEmpty()) {
                return
            }
            isLoadingOscar = true
            notifyDataSetChanged()
        } else {
            // Tắt skeleton nếu đang ở trạng thái loading
            if (isLoadingOscar) {
                isLoadingOscar = false
                notifyDataSetChanged()
            }
        }
    }

    fun getActualListSize(): Int {
        return listData.size
    }

    private fun startSkeletonAnimationOscar(bindingOscar: ViewBinding) {
        if (bindingOscar !is ItemTrendingExpandSkeletonBinding) {
            return
        }

        val viewsOscar = listOf(
            bindingOscar.skeletonPosterOscar,
            bindingOscar.skeletonTagOscar,
            bindingOscar.skeletonRatingOscar,
            bindingOscar.skeletonFavoriteOscar
        )
        for (viewOscar in viewsOscar) {
            val animatorOscar = AnimatorInflater.loadAnimator(
                viewOscar.context, R.animator.skeleton_pulse
            )
            animatorOscar.setTarget(viewOscar)
            animatorOscar.start()
        }
    }
}