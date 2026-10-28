package com.demo.miniflicks.dramaseries.shortstory.util
import android.R
import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView



class AlwaysMarqueeTextView2Oscar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {

    init {
        isSingleLine = true
        ellipsize = TextUtils.TruncateAt.MARQUEE
        marqueeRepeatLimit = -1
        isSelected = true
        isFocusable = true
        isFocusableInTouchMode = true
        setHorizontallyScrolling(true)

        attrs?.let { attributeSet ->
            val typedArrayOscar =
                context.obtainStyledAttributes(attributeSet, intArrayOf(R.attr.textAllCaps))
            val allCapsOscar = typedArrayOscar.getBoolean(0, false)
            typedArrayOscar.recycle()

            if (allCapsOscar) {
                text = text?.toString()?.uppercase()
            }
        }
    }

    override fun isFocused(): Boolean = true

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isSelected = true
    }
}