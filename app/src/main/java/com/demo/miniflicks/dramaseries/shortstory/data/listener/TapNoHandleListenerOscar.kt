package com.demo.miniflicks.dramaseries.shortstory.data.listener
import android.view.View


abstract class TapNoHandleListenerOscar : View.OnClickListener {

    override fun onClick(v: View?) {
        onTapOscar(v)
    }

    abstract fun onTapOscar(vOscar: View?)
}