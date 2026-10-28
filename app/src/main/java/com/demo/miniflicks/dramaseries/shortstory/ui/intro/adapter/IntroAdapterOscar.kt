package com.demo.miniflicks.dramaseries.shortstory.ui.intro.adapter
import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.model.IntroModelOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemIntroOldBinding


class IntroAdapterOscar : BaseAdapterOscar<IntroModelOscar, ItemIntroOldBinding>() {

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
    ): ItemIntroOldBinding {
        return ItemIntroOldBinding.inflate(inflater, parent, false)
    }

    override fun addListData(newList: MutableList<IntroModelOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun setData(binding: ItemIntroOldBinding, item: IntroModelOscar, layoutPosition: Int) {
        binding.tvTitleOscar.text = context!!.getString(item.title)
        binding.tvContentOscar.text = context!!.getString(item.content)
        context?.let { Glide.with(it).load(it.getDrawable(item.image)).into(binding.ivIntroOscar) }
    }

    override fun getItemCount(): Int = listData.size

}