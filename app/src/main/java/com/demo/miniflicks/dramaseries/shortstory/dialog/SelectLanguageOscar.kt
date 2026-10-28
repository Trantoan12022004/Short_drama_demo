package com.demo.miniflicks.dramaseries.shortstory.dialog
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.base.BaseDialogOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.DialogSelectLanguageBinding


class SelectLanguageOscar(
    context: Context,
    private val isSubtitleEnabledOscar: Boolean = false,
    private val onSelectOscar: (isSelected: Boolean) -> Unit
) : BaseDialogOscar<DialogSelectLanguageBinding>(context, true) {

    // Biến lưu trạng thái ngôn ngữ được chọn
    private var isEnglishSelectedOscar: Boolean = false

    override fun setBinding(): DialogSelectLanguageBinding {
        return DialogSelectLanguageBinding.inflate(layoutInflater)
    }

    init {
        // Đồng bộ trạng thái và hiển thị UI sau khi tham số constructor đã sẵn sàng
        isEnglishSelectedOscar = isSubtitleEnabledOscar
        updateEnglishRadioOscar()
    }

    override fun initView() {
        super.initView()
        // Không cập nhật UI phụ thuộc vào tham số lớp con ở đây vì BaseDialogOscar gọi trước khi lớp con khởi tạo
    }

    override fun bindView() {
        super.bindView()

        // Xử lý sự kiện click vào dòng Subtitle English
        binding.subEnOscar.tap {
            toggleLanguageSelectionOscar()
        }

        // Xử lý nút đóng dialog không lưu
        binding.btnCloseOscar.tap {
            dismiss()
        }

        // Xử lý nút Done để lưu lựa chọn và đóng dialog
        binding.btnDoneOscar.tap {
            onSelectOscar(isEnglishSelectedOscar)
            dismiss()
        }
    }

    // Đảo trạng thái chọn ngôn ngữ
    private fun toggleLanguageSelectionOscar() {
        isEnglishSelectedOscar = !isEnglishSelectedOscar
        updateEnglishRadioOscar()
    }

    // Cập nhật icon Radio theo trạng thái chọn
    private fun updateEnglishRadioOscar() {
        if (isEnglishSelectedOscar) {
            binding.rbEnglishOscar.setImageResource(R.drawable.ic_radio_checked)
        } else {
            binding.rbEnglishOscar.setImageResource(R.drawable.ic_radio_unchecked)
        }
    }
}