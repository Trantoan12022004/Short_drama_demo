package com.demo.miniflicks.dramaseries.shortstory.dialog.rate
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.base.BaseDialogOscar
import com.demo.miniflicks.dramaseries.shortstory.base.tap
import com.demo.miniflicks.dramaseries.shortstory.R
import com.demo.miniflicks.dramaseries.shortstory.databinding.DialogRatingAppBinding


class RatingDialogOscar(
    private val contextOscar: Context,
    var onSend: (String) -> Unit,
    var onRate: () -> Unit,
) : BaseDialogOscar<DialogRatingAppBinding>(contextOscar, false) {

    override fun setBinding(): DialogRatingAppBinding {
        return DialogRatingAppBinding.inflate(layoutInflater)
    }

    override fun initView() {
        binding.rtbOscar.setOnRatingBarChangeListener { _, _, _ ->
            when (java.lang.String.valueOf(binding.rtbOscar.rating)) {
                "1.0" -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.thank_you)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_1)
                }

                "2.0" -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.thank_you)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_2)
                }

                "3.0" -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.thank_you)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_3)
                }

                "4.0" -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.thank_you)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_4)
                }

                "5.0" -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.thank_you)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_5)
                }

                else -> {
                    binding.btnRateUsOscar.text = contextOscar.resources.getString(R.string.rate_us)
                    binding.imgIconOscar.setImageResource(R.drawable.rating_0)
                }
            }
        }
    }

    override fun bindView() {
        binding.btnRateUsOscar.tap {
            if (binding.rtbOscar.rating <= 3.0) {
                onSend.invoke(rating)
            } else {
                onRate.invoke()
            }
            dismiss()
        }
        binding.btnNotNowOscar.tap { dismiss() }
    }

    val rating: String
        get() = java.lang.String.valueOf(this.binding.rtbOscar.rating)
}