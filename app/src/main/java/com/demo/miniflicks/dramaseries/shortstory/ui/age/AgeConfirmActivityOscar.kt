package com.demo.miniflicks.dramaseries.shortstory.ui.age
import com.demo.miniflicks.dramaseries.shortstory.base.BaseActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.ui.home.MainActivity
import com.demo.miniflicks.dramaseries.shortstory.util.GlobalData
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.databinding.ActivityAgeConfirmBinding


class AgeConfirmActivityOscar : BaseActivityOscar<ActivityAgeConfirmBinding>(ActivityAgeConfirmBinding::inflate) {

    override fun getData() {
        super.getData()
        // Kiểm tra xem người dùng đã từng xác nhận độ tuổi trước đó chưa
        val isAgeConfirmedOscar = SharedPreUtilsOscar.getInstance().getBoolean(GlobalData.IS_AGE_CONFIRMOscar, false)
        if (isAgeConfirmedOscar) {
            // Nếu đã xác nhận rồi thì bỏ qua màn này và chuyển thẳng vào MainActivity
            startNextActivity(MainActivity::class.java, null)
            finishAffinity()
        }
    }

    override fun bindView() {
        // Người dùng chọn trên 12 tuổi (Đồng ý)
        binding.btnAgreeOscar.tap {
            // Lưu trạng thái đã xác nhận vào SharedPreferences
            SharedPreUtilsOscar.getInstance().setBoolean(GlobalData.IS_AGE_CONFIRMOscar, true)
            startNextActivity(MainActivity::class.java, null)
            finishAffinity()
        }

        // Người dùng chọn chưa đủ 12 tuổi (Từ chối)
        binding.btnDenyOscar.tap {
            startNextActivity(AgeRestrictionActivityOscar::class.java, null)
            finishAffinity()
        }
    }

    override fun onBack() {
        finishAffinity()
    }
}