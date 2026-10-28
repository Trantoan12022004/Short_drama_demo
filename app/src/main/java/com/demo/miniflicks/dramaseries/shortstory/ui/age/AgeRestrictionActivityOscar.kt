package com.demo.miniflicks.dramaseries.shortstory.ui.age
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityAgeRestrictionBinding


class AgeRestrictionActivityOscar : BaseActivityOscar<ActivityAgeRestrictionBinding>(ActivityAgeRestrictionBinding::inflate) {
    override fun bindView() {
        binding.btnTryAgainOscar.tap {
            startNextActivity(AgeConfirmActivityOscar::class.java, null)
            finishAffinity()
        }
    }

    override fun onBack() {
        finishAffinity()
    }

}