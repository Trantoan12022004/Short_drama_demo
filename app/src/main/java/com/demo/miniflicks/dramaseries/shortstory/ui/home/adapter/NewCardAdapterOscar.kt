package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.widget.DiagonalImageViewOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemNewBinding
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTrendingMiniSkeletonBinding
import java.util.Locale


class NewCardAdapterOscar : BaseAdapterOscar<MovieCardOscar, ViewBinding>() {

    var onClick: ((MovieCardOscar) -> Unit)? = null

    // Cờ loading: true = hiện skeleton khi chưa có dữ liệu, false = hiện data thật
    private var isLoadingOscar: Boolean = true

    // Số lượng skeleton item khi đang load (vừa vặn chiều ngang màn hình)
    private val skeletonCountOscar: Int = 5

    companion object {
        const val VIEW_TYPE_SKELETONOscar = 0
        const val VIEW_TYPE_DATAOscar = 1
    }

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        val inflatedBindingOscar: ViewBinding
        if (viewType == VIEW_TYPE_SKELETONOscar) {
            inflatedBindingOscar = ItemTrendingMiniSkeletonBinding.inflate(inflater, parent, false)
        } else {
            inflatedBindingOscar = ItemNewBinding.inflate(inflater, parent, false)
        }
        return inflatedBindingOscar
    }

    override fun addListData(newList: MutableList<MovieCardOscar>) {
        val maxItemCountOscar = 5
        val takeCountOscar: Int
        if (newList.size < maxItemCountOscar) {
            takeCountOscar = newList.size
        } else {
            takeCountOscar = maxItemCountOscar
        }
        val limitedListOscar = newList.subList(0, takeCountOscar)

        // Nếu danh sách hiện tại không ở chế độ loading và cùng danh sách phim thì không cần re-bind
        if (!isLoadingOscar && listData.size == limitedListOscar.size) {
            var hasDifferenceOscar = false
            for (index in 0 until listData.size) {
                val currentMovieOscar = listData[index]
                val newMovieOscar = limitedListOscar[index]
                if (currentMovieOscar.id != newMovieOscar.id || currentMovieOscar.viewCount != newMovieOscar.viewCount) {
                    hasDifferenceOscar = true
                    break
                }
            }
            if (!hasDifferenceOscar) {
                return
            }
        }

        listData.clear()
        listData.addAll(limitedListOscar)
        isLoadingOscar = false
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
        } else {
            itemCountOscar = listData.size
        }
        return itemCountOscar
    }

    override fun onBindViewHolder(holder: ViewHolderOscar, position: Int) {
        val viewTypeOscar: Int = getItemViewType(position)
        if (viewTypeOscar == VIEW_TYPE_SKELETONOscar) {
            startSkeletonAnimationOscar(holder.binding)
        } else {
            if (position < 0 || position >= listData.size) {
                return
            }
            val itemOscar: MovieCardOscar = listData[position]
            holder.bindView(itemOscar)
            // Truyền vị trí position chính xác vào setData thay vì dùng layoutPosition
            setData(holder.binding, itemOscar, position)
        }
    }


    override fun setData(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        if (binding !is ItemNewBinding) {
            return
        }
        val currentContextOscar = binding.root.context
        // Load ảnh poster kèm ProgressBar xoay tròn loading độc lập
        binding.ivCardMovieOscar.loadImageWithProgress(
            url = item.posterUrl,
            progressBar = binding.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )
        // Cắt chéo so le: vị trí chẵn dốc xuống phải (\), vị trí lẻ dốc xuống trái (/)
        if (layoutPosition % 2 == 0) {
            binding.ivCardMovieOscar.slopeDirection = DiagonalImageViewOscar.SlopeDirectionOscar.DOWN_RIGHT
        } else {
            binding.ivCardMovieOscar.slopeDirection = DiagonalImageViewOscar.SlopeDirectionOscar.DOWN_LEFT
        }
        // Highlight viền vàng cho bộ phim đầu tiên (vị trí 0)
        if (layoutPosition == 0) {
            binding.ivCardMovieOscar.isHighlighted = true
        } else {
            binding.ivCardMovieOscar.isHighlighted = false
        }
        val viewCountInKOscar: Double = item.viewCount / 1000000.0
        val viewCountTextOscar: String = String.format(Locale.US, "%.1f", viewCountInKOscar)
        binding.tvCountViewOscar.text = currentContextOscar.getString(R.string.view, viewCountTextOscar)
        binding.tvMovieTitleOscar.text = item.title
    }

    override fun onCLick(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        if (binding !is ItemNewBinding) {
            return
        }

        binding.root.tap {
            val listenerOscar = onClick
            if (listenerOscar != null) {
                listenerOscar(item)
            }
        }
    }

    fun showSkeleton(show: Boolean) {
        if (show) {
            // Nếu adapter đã có dữ liệu thật từ trước thì không quay lại skeleton
            // để tránh bị giật hoặc nhấp nháy giao diện khi đồng bộ ngầm
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

    private fun startSkeletonAnimationOscar(bindingOscar: ViewBinding) {
        if (bindingOscar !is ItemTrendingMiniSkeletonBinding) {
            return
        }

        val viewsOscar = listOf(
            bindingOscar.skeletonPosterOscar,
            bindingOscar.skeletonViewCountOscar,
            bindingOscar.skeletonMovieTitleOscar
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