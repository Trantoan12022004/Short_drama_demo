package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.resolvePoster
import com.demo.miniflicks.dramaseries.shortstory.model.resolveTitle
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemSearchBinding
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemSearchSkeletonBinding
import java.util.Locale


class CategoryMovieCardAdapterOscar : BaseAdapterOscar<MovieEntityOscar, ViewBinding>() {

    var onClick: ((MovieEntityOscar) -> Unit)? = null
    // Ngôn ngữ hiện tại để resolve tên/poster, set từ Activity/Fragment trước khi addListData
    var lang: String = "en"

    // Cờ loading: true = hiện skeleton, false = hiện data thật
    private var isLoadingOscar: Boolean = true

    // Số lượng skeleton item khi đang load
    private val skeletonCountOscar: Int = 12

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
            inflatedBindingOscar = ItemSearchSkeletonBinding.inflate(inflater, parent, false)
        } else {
            inflatedBindingOscar = ItemSearchBinding.inflate(inflater, parent, false)
        }
        return inflatedBindingOscar
    }

    override fun addListData(newList: MutableList<MovieEntityOscar>) {
        listData.clear()
        val maxItemCountOscar = 5
        val takeCountOscar: Int
        if (newList.size < maxItemCountOscar) {
            takeCountOscar = newList.size
        } else {
            takeCountOscar = maxItemCountOscar
        }
        val limitedListOscar = newList.subList(0, takeCountOscar)
        listData.addAll(newList)
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
        val viewTypeOscar = getItemViewType(position)
        if (viewTypeOscar == VIEW_TYPE_SKELETONOscar) {
            startSkeletonAnimationOscar(holder.binding)
        } else {
            if (position < 0 || position >= listData.size) {
                return
            }
            val itemOscar = listData[position]
            holder.bindData(itemOscar)
        }
    }

    override fun setData(binding: ViewBinding, item: MovieEntityOscar, layoutPosition: Int) {
        if (binding !is ItemSearchBinding) {
            return
        }

        val currentContextOscar = binding.root.context

        // Resolve poster URL theo ngôn ngữ hiện tại
        val resolvedPosterOscar = item.resolvePoster(lang)
        // Load ảnh poster kèm ProgressBar xoay tròn loading độc lập
        binding.ivCardMovieOscar.loadImageWithProgress(
            url = resolvedPosterOscar,
            progressBar = binding.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )

        val viewCountInKOscar = item.viewCount / 1000000.0
        val viewCountTextOscar = String.format(Locale.US, "%.1f", viewCountInKOscar)
        binding.tvCountViewOscar.text = currentContextOscar.getString(R.string.view, viewCountTextOscar)
        // Resolve tên phim theo ngôn ngữ hiện tại
        binding.tvMovieTitleOscar.text = item.resolveTitle(lang)
    }

    override fun onCLick(binding: ViewBinding, item: MovieEntityOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        if (binding !is ItemSearchBinding) {
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
        if (bindingOscar !is ItemSearchSkeletonBinding) {
            return
        }

        val viewsOscar = listOf(bindingOscar.skeletonPosterOscar, bindingOscar.skeletonViewCountOscar)
        for (viewOscar in viewsOscar) {
            val animatorOscar = AnimatorInflater.loadAnimator(
                viewOscar.context, R.animator.skeleton_pulse
            )
            animatorOscar.setTarget(viewOscar)
            animatorOscar.start()
        }
    }
}