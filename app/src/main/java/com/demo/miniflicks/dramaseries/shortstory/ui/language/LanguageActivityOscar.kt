package com.demo.miniflicks.dramaseries.shortstory.ui.language
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainActivity
import com.demo.miniflicks.dramaseries.shortstory.ui.language.adapter.LanguageAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.util.InsertListManager
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityLanguageOldBinding


class LanguageActivityOscar : BaseActivityOscar<ActivityLanguageOldBinding>(ActivityLanguageOldBinding::inflate) {

    private var codeLangOscar: String? = null

    override fun initView() {
        codeLangOscar = SystemUtilOscar.getPreLanguage(this)
        binding.viewTopOscar.tvToolBarOscar.text = getString(R.string.language)

        binding.rcvLangOscar.apply {
            adapter = LanguageAdapterOscar { codeLangOscar = it }.apply {
                addListData(InsertListManager.getListLanguage(this@LanguageActivityOscar))
                setCheck(codeLangOscar)
            }

        }
    }

    override fun bindView() {
        binding.viewTopOscar.ivLeftOscar.visible()
        binding.viewTopOscar.ivLeftOscar.tap { onBack() }
        binding.viewTopOscar.ivRightOscar.setImageResource(R.drawable.check)
        binding.viewTopOscar.ivRightOscar.tap {
            SystemUtilOscar.saveLocale(this, codeLangOscar)

            onNextActivityOscar()
        }
    }

    private fun onNextActivityOscar() {
        startNextActivity(MainActivity::class.java, null)
        finishAffinity()
    }

}