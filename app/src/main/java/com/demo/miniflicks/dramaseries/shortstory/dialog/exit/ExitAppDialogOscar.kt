package com.demo.miniflicks.dramaseries.shortstory.dialog.exit
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.base.BaseDialogOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.databinding.DialogExitAppBinding


class ExitAppDialogOscar(context: Context, cancelAble: Boolean?, var onClick: () -> Unit) :
    BaseDialogOscar<DialogExitAppBinding>(context, cancelAble == true) {

    override fun setBinding(): DialogExitAppBinding {
        return DialogExitAppBinding.inflate(layoutInflater)
    }

    override fun initView() {}

    override fun bindView() {
        binding.btnCancelQuitAppOscar.tap { dismiss() }

        binding.btnQuitAppOscar.tap {
            dismiss()
            onClick.invoke()
        }
    }

}