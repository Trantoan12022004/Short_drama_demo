package com.demo.miniflicks.dramaseries.shortstory.base
import android.app.Dialog
import android.content.Context
import android.view.Window
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R


abstract class BaseDialogOscar<VB : ViewBinding>(
    context: Context,
    canAble: Boolean,
) :
    Dialog(context, R.style.BaseDialog) {

    var binding: VB
    protected abstract fun setBinding(): VB

    init {
        SystemUtilOscar.setLocale(context)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = setBinding()
        setContentView(binding.root)
        setCancelable(canAble)
        initView()
        bindView()
    }

    open fun initView() {

    }

    open fun bindView() {
    }




}