package com.demo.miniflicks.dramaseries.shortstory.ui.home.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.CategoryEntityOscar
import com.demo.miniflicks.dramaseries.shortstory.model.resolveName
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemTagBinding


class TagAdapterOscar: BaseAdapterOscar<CategoryEntityOscar, ItemTagBinding>() {
    var onClick : ((CategoryEntityOscar) -> Unit)? = null
    // Ngôn ngữ hiện tại để resolve tên thể loại, set từ Fragment trước khi addListData
    var lang: String = "en"
    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ItemTagBinding {
        return ItemTagBinding.inflate(inflater, parent, false)
    }

    override fun addListData(newList: MutableList<CategoryEntityOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    override fun setData(
        binding: ItemTagBinding,
        item: CategoryEntityOscar,
        layoutPosition: Int
    ) {
        // Resolve tên thể loại theo ngôn ngữ hiện tại
        binding.tvTagOscar.text = item.resolveName(lang)
    }

    override fun onCLick(binding: ItemTagBinding, item: CategoryEntityOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        binding.root.tap {
            val listenerOscar = onClick
            if (listenerOscar != null) {
                listenerOscar(item)
            }
        }
    }
}