package com.demo.miniflicks.dramaseries.shortstory.ui.welcome
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityWelcomeOldBinding


class WelcomeActivityOscar : BaseActivityOscar<ActivityWelcomeOldBinding>(ActivityWelcomeOldBinding::inflate) {

    override fun initView() {
        // Có thể thêm hiệu ứng animation cho icon kim cương ở đây nếu muốn
    }

    override fun bindView() {
        binding.tvContinueOscar.tap {
            finishThisActivity()
        }
    }

    override fun onBack() {
        finishAffinity()
    }
}