package com.demo.miniflicks.dramaseries.shortstory.ui.language
import android.view.Gravity
import android.widget.Toast
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.ui.intro.IntroActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.language.adapter.LanguageAdapterOscar
import com.demo.miniflicks.dramaseries.shortstory.util.InsertListManager
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityLanguageStartOldBinding


class LanguageStartActivityOscar :
    BaseActivityOscar<ActivityLanguageStartOldBinding>(ActivityLanguageStartOldBinding::inflate) {

    private var codeLangOscar: String? = null
    private var toastOscar: Toast? = null

    override fun getData() {
        super.getData()

        codeLangOscar = SystemUtilOscar.getPreLanguage(this)
    }

    override fun initView() {
        binding.viewTopOscar.apply {
            tvToolBarOscar.text = getString(R.string.choose_language)
//            setTextColor(tvToolBarOscar, "#FFFFF")
            tvToolBarOscar.gravity = Gravity.START
            ivRightOscar.visible()
        }


        binding.rcvLangStartOscar.apply {
            adapter = LanguageAdapterOscar {
                codeLangOscar = it
            }.apply {
                addListData(InsertListManager.getListLanguage(this@LanguageStartActivityOscar))
                if (codeLangOscar != ""){
                    setCheck(codeLangOscar)
                }
            }
        }
    }

    override fun bindView() {

        binding.viewTopOscar.ivRightOscar.tap {
            SystemUtilOscar.saveLocale(this, codeLangOscar)

            if (codeLangOscar != "") {
                SystemUtilOscar.saveLocale(this, codeLangOscar)
                startNextActivityOscar()
            } else {
                if (toastOscar != null) toastOscar!!.cancel()
                toastOscar = Toast.makeText(
                    this, getString(R.string.please_choose_a_language), Toast.LENGTH_SHORT
                )
                toastOscar!!.show()
            }
        }
    }

    private fun startNextActivityOscar() {
        startNextActivity(IntroActivityOscar::class.java, null)
        finish()
    }

    override fun onBack() {
        finishAffinity()
    }
}