package com.demo.miniflicks.dramaseries.shortstory.base
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding


abstract class BaseAdapterOscar<T, VB : ViewBinding> :
    RecyclerView.Adapter<BaseAdapterOscar<T, VB>.ViewHolderOscar>() {

    private var bindingOscar: VB? = null

    abstract fun setBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): VB

    abstract fun addListData(newList: MutableList<T>)

    abstract fun setData(binding: VB, item: T, layoutPosition: Int)

    val listData: MutableList<T> = mutableListOf()
    var context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderOscar {
        context = parent.context
        bindingOscar = setBinding(LayoutInflater.from(context), parent, viewType)
        return ViewHolderOscar(bindingOscar!!)
    }

    override fun onBindViewHolder(holder: ViewHolderOscar, position: Int) {
        holder.bindData(listData[position])
    }

    override fun getItemCount(): Int = listData.size

    inner class ViewHolderOscar(val binding: VB) : BaseViewHolderOscar<T>(binding) {

        override fun bindData(obj: T) {
            bindView(obj)
            setData(binding, obj, layoutPosition)
        }

        override fun bindView(obj: T) {
            onCLick(binding, obj, layoutPosition)
        }

    }

    open fun onCLick(binding: VB, item: T, layoutPosition: Int) {

    }
}