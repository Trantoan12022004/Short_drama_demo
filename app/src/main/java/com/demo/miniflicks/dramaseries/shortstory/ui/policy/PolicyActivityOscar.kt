package com.demo.miniflicks.dramaseries.shortstory.ui.policy
import android.annotation.SuppressLint
import com.demo.miniflicks.dramaseries.shortstory.ads.IsNetWork
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.inVisible
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.util.SettingManager
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityPolicyBinding


class PolicyActivityOscar : BaseActivityOscar<ActivityPolicyBinding>(ActivityPolicyBinding::inflate) {

    @SuppressLint("SetJavaScriptEnabled")
    override fun initView() {
        binding.viewTopOscar.tvToolBarOscar.text = getString(R.string.privacy_policy)
        binding.viewTopOscar.ivRightOscar.inVisible()
        binding.viewTopOscar.ivLeftOscar.visible()

        if (SettingManager.linkPolicyOscar != "" && IsNetWork.haveNetworkConnection(this)) {
            binding.webViewOscar.visible()
            binding.lnNoInternetOscar.gone()

            binding.webViewOscar.settings.javaScriptEnabled = true
            binding.webViewOscar.loadUrl(SettingManager.linkPolicyOscar)
        } else {
            binding.webViewOscar.gone()
            binding.lnNoInternetOscar.visible()
        }
    }

    override fun bindView() {
        binding.viewTopOscar.ivLeftOscar.tap { onBack() }
    }

}