package com.demo.miniflicks.dramaseries.shortstory.ui.play_video.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.play_video.model.EpisodeNumberItemOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemNumberBinding


class EpisodeNumberAdapterOscar : BaseAdapterOscar<EpisodeNumberItemOscar, ItemNumberBinding>(){

    var onEpisodeClick: ((EpisodeNumberItemOscar) -> Unit)? = null
    // Vị trí tập đang phát để tô đậm ô tương ứng
    var currentIndex: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemNumberBinding {
        return ItemNumberBinding.inflate(inflater, parent, false)
    }

    override fun addListData(newList: MutableList<EpisodeNumberItemOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    override fun setData(
        binding: ItemNumberBinding,
        item: EpisodeNumberItemOscar,
        layoutPosition: Int
    ) {
        val contextOscar = binding.root.context
        val displayNumberOscar = item.number + 1

        if (item.hasVideoUrl) {
            binding.tvNumberOscar.visible()
            binding.ivLockOscar.gone()
            binding.tvNumberOscar.text = displayNumberOscar.toString()
        } else {
            binding.tvNumberOscar.gone()
            binding.ivLockOscar.visible()
        }

        // Tô đậm màu chữ cho tập đang phát
        if (item.number == currentIndex) {
            binding.tvNumberOscar.setTextColor(ContextCompat.getColor(contextOscar, R.color.Orange_50))
        } else {
            binding.tvNumberOscar.setTextColor(ContextCompat.getColor(contextOscar, R.color.white))
        }

    }

    override fun onCLick(binding: ItemNumberBinding, item: EpisodeNumberItemOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        binding.root.tap {
            if (item.hasVideoUrl) {
                val listenerOscar = onEpisodeClick
                if (listenerOscar != null) {
                    listenerOscar(item)
                }
            }
        }
    }
}