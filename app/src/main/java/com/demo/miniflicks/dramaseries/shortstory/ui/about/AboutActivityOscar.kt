package com.demo.miniflicks.dramaseries.shortstory.ui.about
import android.annotation.SuppressLint
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.inVisible
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.policy.PolicyActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.BuildConfig
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityAboutBinding


class AboutActivityOscar : BaseActivityOscar<ActivityAboutBinding>(ActivityAboutBinding::inflate) {

    @SuppressLint("SetTextI18n")
    override fun initView() {
        binding.viewTopOscar.tvToolBarOscar.text = getString(R.string.about)
        binding.viewTopOscar.ivRightOscar.inVisible()
        binding.viewTopOscar.ivLeftOscar.visible()

        binding.tvVersionOscar.text = getString(R.string.version) + " ${BuildConfig.VERSION_NAME}"
    }

    override fun bindView() {
        binding.viewTopOscar.ivLeftOscar.tap { onBack() }

        binding.tvPolicyOscar.tap { startNextActivity(PolicyActivityOscar::class.java, null) }
    }

}