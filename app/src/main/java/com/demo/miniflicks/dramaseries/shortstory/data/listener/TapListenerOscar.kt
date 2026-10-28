package com.demo.miniflicks.dramaseries.shortstory.data.listener
import android.os.SystemClock
import android.view.View


abstract class TapListenerOscar : View.OnClickListener {

    companion object {
        private const val TIME_WAITOscar = 500L
    }

    private var lastClickOscar: Long = 0
    private var nowOscar: Long = 0L

    override fun onClick(v: View?) {
        nowOscar = SystemClock.elapsedRealtime();
        if (nowOscar - lastClickOscar > TIME_WAITOscar) {
            onTapOscar(v)
        }
        lastClickOscar = nowOscar
    }

    abstract fun onTapOscar(vOscar: View?)
}