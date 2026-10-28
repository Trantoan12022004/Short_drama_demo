package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.animation.AnimatorInflater
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.loadImageWithProgress
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.MovieCardOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTrendingMiniBinding
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTrendingMiniSkeletonBinding
import java.util.Locale


class MovieCardAdapterOscar : BaseAdapterOscar<MovieCardOscar, ViewBinding>() {

    var onClick: ((MovieCardOscar) -> Unit)? = null

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
            inflatedBindingOscar = ItemTrendingMiniSkeletonBinding.inflate(inflater, parent, false)
        } else {
            inflatedBindingOscar = ItemTrendingMiniBinding.inflate(inflater, parent, false)
        }
        return inflatedBindingOscar
    }

    override fun addListData(newList: MutableList<MovieCardOscar>) {
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

    override fun setData(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        if (binding !is ItemTrendingMiniBinding) {
            return
        }

        val currentContextOscar = binding.root.context

        // Load ảnh poster kèm ProgressBar xoay tròn loading độc lập
        binding.ivCardMovieOscar.loadImageWithProgress(
            url = item.posterUrl,
            progressBar = binding.pbLoadingOscar,
            errorResId = R.drawable.card_demo
        )

        val viewCountInKOscar = item.viewCount / 1000000.0
        val viewCountTextOscar = String.format(Locale.US, "%.1f", viewCountInKOscar)
        binding.tvCountViewOscar.text = currentContextOscar.getString(R.string.view, viewCountTextOscar)
        binding.tvMovieTitleOscar.text = item.title
    }

    override fun onCLick(binding: ViewBinding, item: MovieCardOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        if (binding !is ItemTrendingMiniBinding) {
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
        isLoadingOscar = show
        notifyDataSetChanged()
    }

    private fun startSkeletonAnimationOscar(bindingOscar: ViewBinding) {
        if (bindingOscar !is ItemTrendingMiniSkeletonBinding) {
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