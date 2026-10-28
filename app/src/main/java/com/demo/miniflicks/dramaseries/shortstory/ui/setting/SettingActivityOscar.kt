package com.demo.miniflicks.dramaseries.shortstory.ui.setting
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.about.AboutActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.language.LanguageActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.InsertListManager
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.util.rateApp
import com.demo.miniflicks.dramaseries.shortstory.util.shareApp
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivitySettingsBinding


class SettingActivityOscar : BaseActivityOscar<ActivitySettingsBinding>(ActivitySettingsBinding::inflate) {


    override fun getData() {
        val codeLangOscar = SystemUtilOscar.getPreLanguage(this)
        binding.tvLangOscar.text = InsertListManager.getListLanguage(this@SettingActivityOscar).find { it.code == codeLangOscar }?.name ?: ""

        if (SharedPreUtilsOscar.getInstance().isRated(this)) {
            binding.btnRateUsOscar.gone()
        }
    }

    override fun initView() {
        binding.viewTopOscar.tvToolBarOscar.text = getString(R.string.settings)
        // binding.viewTopOscar.tvToolBarOscar.isAllCaps = true
        binding.viewTopOscar.ivRightOscar.gone()
        binding.viewTopOscar.ivLeftOscar.visible()

    }

    override fun bindView() {

        binding.btnLanguageOscar.tap { startNextActivity(LanguageActivityOscar::class.java, null) }

        binding.btnShareOscar.tap { shareApp() }

        binding.btnRateUsOscar.tap { rateApp(binding.btnRateUsOscar) }
        binding.viewTopOscar.ivLeftOscar.tap{
            onBack()
        }

        binding.btnAboutOscar.tap { startNextActivity(AboutActivityOscar::class.java, null) }
    }


}