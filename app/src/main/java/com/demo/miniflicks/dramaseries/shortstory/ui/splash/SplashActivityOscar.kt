package com.demo.miniflicks.dramaseries.shortstory.ui.splash
import android.os.Handler
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.language.LanguageStartActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivitySplashOldBinding


class SplashActivityOscar : BaseActivityOscar<ActivitySplashOldBinding>(ActivitySplashOldBinding::inflate) {


    override fun initView() {
        SharedPreUtilsOscar.getInstance().setCountOpenApp(this)

        Handler(mainLooper).postDelayed({
            startNextActivityOscar()
        }, 1500)
    }

    private fun startNextActivityOscar() {
        startNextActivity(LanguageStartActivityOscar::class.java, null)
        finishAffinity()
    }

    override fun onBack() {}
}