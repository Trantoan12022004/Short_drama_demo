package com.demo.miniflicks.dramaseries.shortstory.ui.language.adapter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.demo.miniflicks.dramaseries.shortstory.base.BaseAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.model.LanguageModelOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ItemLanguageOldBinding


class LanguageAdapterOscar(var onClick: (String) -> Unit) :
    BaseAdapterOscar<LanguageModelOscar, ItemLanguageOldBinding>() {

    override fun setBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int,
    ): ItemLanguageOldBinding {
        return ItemLanguageOldBinding.inflate(inflater, parent, false)
    }

    override fun addListData(newList: MutableList<LanguageModelOscar>) {
        listData.clear()
        listData.addAll(newList)
        notifyDataSetChanged()
    }

    override fun setData(binding: ItemLanguageOldBinding, item: LanguageModelOscar, layoutPosition: Int) {
        binding.rdbCheckOscar.gone()
        binding.tvLangOscar.text = item.name

        val currentContextOscar = binding.root.context
        if (item.active) {
            // Khi item được chọn: đặt background active và chữ màu cam
            binding.layoutItemOscar.setBackgroundResource(R.drawable.bg_lang_item_s)
            val activeColorOscar = ContextCompat.getColor(currentContextOscar, R.color.Orange_50)
            binding.tvLangOscar.setTextColor(activeColorOscar)
        } else {
            // Khi item không chọn: đặt background mặc định và chữ màu trắng
            binding.layoutItemOscar.setBackgroundResource(R.drawable.bg_lang_item_sn)
            val inactiveColorOscar = ContextCompat.getColor(currentContextOscar, R.color.white)
            binding.tvLangOscar.setTextColor(inactiveColorOscar)
        }

        context?.let {
            Glide.with(it).asBitmap().load(item.image).into(binding.icLangOscar)
        }
    }

    override fun getItemCount(): Int = listData.size

    override fun onCLick(binding: ItemLanguageOldBinding, item: LanguageModelOscar, layoutPosition: Int) {
        super.onCLick(binding, item, layoutPosition)
        binding.layoutItemOscar.tap {
            setCheck(item.code)
            onClick(item.code)
        }
    }

    fun setCheck(code: String?) {
        for (item in listData) {
            item.active = item.code == code
        }
        notifyDataSetChanged()
    }

}