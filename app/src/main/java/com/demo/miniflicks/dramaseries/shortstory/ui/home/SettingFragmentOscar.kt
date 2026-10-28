package com.demo.miniflicks.dramaseries.shortstory.ui.home
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.b9default.ui.language.LanguageSettingActivity
import com.demo.miniflicks.dramaseries.shortstory.base.BaseFragmentOscar
import com.demo.miniflicks.dramaseries.shortstory.base.gone
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.base.visible
import com.demo.miniflicks.dramaseries.shortstory.model.LanguageModelOscar
import com.demo.miniflicks.dramaseries.shortstory.ui.about.AboutActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.util.InsertListManager
import com.demo.miniflicks.dramaseries.shortstory.util.SharedPreUtilsOscar
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.util.rateApp
import com.demo.miniflicks.dramaseries.shortstory.util.shareApp
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.FragmentSettingBinding


class SettingFragmentOscar : BaseFragmentOscar<FragmentSettingBinding>() {

    override fun setBinding(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        saveInstanceState: Bundle?
    ): FragmentSettingBinding {
        return FragmentSettingBinding.inflate(inflater!!, container, false)
    }

    override fun getData() {
        // Cập nhật thông tin dữ liệu cho màn hình cài đặt
        updateSettingDataOscar()
    }

    override fun onResume() {
        super.onResume()
        // Cập nhật lại dữ liệu khi người dùng quay lại từ màn hình khác (ví dụ: đổi ngôn ngữ)
        updateSettingDataOscar()
    }

    override fun initView() {
        // Cấu hình tiêu đề Toolbar
        binding.viewTopOscar.tvToolBarOscar.text = getString(R.string.settings)

        // Ẩn nút check bên phải
        binding.viewTopOscar.ivRightOscar.gone()

        // Ẩn nút back bên trái vì đây là màn hình tab chính
        binding.viewTopOscar.ivLeftOscar.gone()
    }

    override fun bindView() {
        // Xử lý click chọn ngôn ngữ
        binding.btnLanguageOscar.tap {
            startNextActivity(LanguageSettingActivity::class.java, null)
        }

        // Xử lý click chia sẻ ứng dụng
        binding.btnShareOscar.tap {
            requireActivity().shareApp()
        }

        // Xử lý click đánh giá ứng dụng
        binding.btnRateUsOscar.tap {
            requireActivity().rateApp(binding.btnRateUsOscar)
        }

        // Xử lý click chuyển sang màn hình About
        binding.btnAboutOscar.tap {
            startNextActivity(AboutActivityOscar::class.java, null)
        }
    }

    private fun updateSettingDataOscar() {
        // Lấy mã ngôn ngữ đã lưu
        val codeLangOscar: String = SystemUtilOscar.getPreLanguage(requireContext())

        // Lấy danh sách ngôn ngữ được hỗ trợ
        val listLanguageOscar: List<LanguageModelOscar> = InsertListManager.getListLanguage(requireContext())

        // Tìm kiếm tên ngôn ngữ tương ứng
        val currentLanguageOscar: LanguageModelOscar? = listLanguageOscar.find { itemOscar ->
            itemOscar.code == codeLangOscar
        }

        if (currentLanguageOscar != null) {
            binding.tvLangOscar.text = currentLanguageOscar.name
        } else {
            binding.tvLangOscar.text = ""
        }

        // Kiểm tra trạng thái đã đánh giá ứng dụng chưa
        val isRatedOscar: Boolean = SharedPreUtilsOscar.getInstance().isRated(requireContext())
        if (isRatedOscar) {
            // Ẩn nút rate và đường viền phân cách nếu đã đánh giá
            binding.btnRateUsOscar.gone()
            binding.viewRateUsOscar.gone()
        } else {
            binding.btnRateUsOscar.visible()
            binding.viewRateUsOscar.visible()
        }
    }
}