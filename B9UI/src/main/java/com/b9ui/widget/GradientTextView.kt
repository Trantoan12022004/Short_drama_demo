package com.b9ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import com.b9ui.R

class GradientTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        const val ORIENTATION_LEFT_TO_RIGHTOscar: Int = 0
        const val ORIENTATION_BOTTOM_TO_TOPOscar: Int = 1
        const val ORIENTATION_TOP_TO_BOTTOMOscar: Int = 2
        const val ORIENTATION_RIGHT_TO_LEFTOscar: Int = 3
    }

    // Màu bắt đầu và màu kết thúc của gradient
    private var startColorOscar: Int = "#FE8C00".toColorInt()
    private var endColorOscar: Int = "#F12711".toColorInt()

    // Hướng của dải màu gradient (mặc định: từ dưới lên trên)
    private var gradientOrientationOscar: Int = ORIENTATION_BOTTOM_TO_TOPOscar

    // Biến lưu trữ Shader hiện tại
    private var gradientShaderOscar: LinearGradient? = null

    // Biến lưu trữ giá trị đo đạc trước đó để tránh khởi tạo Shader liên tục
    private var previousWidthOscar: Int = 0
    private var previousHeightOscar: Int = 0
    private var previousTextOscar: String = ""

    init {
        // Đọc các thuộc tính tùy chỉnh từ XML nếu có
        if (attrs != null) {
            val typedArrayOscar = context.obtainStyledAttributes(attrs, R.styleable.GradientTextView, defStyleAttr, 0)

            val defaultStartOscar = "#FE8C00".toColorInt()
            val defaultEndOscar = "#F12711".toColorInt()

            startColorOscar = typedArrayOscar.getColor(R.styleable.GradientTextView_startColor, defaultStartOscar)
            endColorOscar = typedArrayOscar.getColor(R.styleable.GradientTextView_endColor, defaultEndOscar)
            gradientOrientationOscar = typedArrayOscar.getInt(
                R.styleable.GradientTextView_gradientOrientation,
                ORIENTATION_BOTTOM_TO_TOPOscar
            )

            typedArrayOscar.recycle()
        }
    }

    override fun onDraw(canvas: Canvas) {
        // Cập nhật lại Shader trước khi vẽ text nếu có thay đổi
        updateShaderIfNeededOscar()
        super.onDraw(canvas)
    }

    // Phương thức thiết lập màu gradient từ code
    fun setGradientColors(newStartColor: Int, newEndColor: Int) {
        startColorOscar = newStartColor
        endColorOscar = newEndColor
        gradientShaderOscar = null
        invalidate()
    }

    // Phương thức thiết lập hướng gradient từ code
    fun setGradientOrientation(newOrientation: Int) {
        gradientOrientationOscar = newOrientation
        gradientShaderOscar = null
        invalidate()
    }

    // Cập nhật Shader dựa trên vị trí chính xác của các ký tự text
    private fun updateShaderIfNeededOscar() {
        val currentTextOscar = text.toString()
        val currentWidthOscar = width
        val currentHeightOscar = height

        // Nếu nội dung trống hoặc kích thước view bằng 0 thì hủy shader
        if (currentTextOscar.isEmpty() || currentWidthOscar == 0 || currentHeightOscar == 0) {
            paint.shader = null
            return
        }

        // Kiểm tra xem có cần cập nhật lại Shader hay không
        val isTextChangedOscar = currentTextOscar != previousTextOscar
        val isSizeChangedOscar = currentWidthOscar != previousWidthOscar || currentHeightOscar != previousHeightOscar
        val isShaderNullOscar = gradientShaderOscar == null

        if (isTextChangedOscar || isSizeChangedOscar || isShaderNullOscar) {
            previousTextOscar = currentTextOscar
            previousWidthOscar = currentWidthOscar
            previousHeightOscar = currentHeightOscar

            val currentLayoutOscar = layout
            var textStartXOscar = 0f
            var textEndXOscar = currentWidthOscar.toFloat()
            var textStartYOscar = 0f
            var textEndYOscar = currentHeightOscar.toFloat()

            // Nếu layout của TextView đã sẵn sàng, tính toạ độ dựa trên từng dòng chữ
            if (currentLayoutOscar != null) {
                val lineCountOscar = currentLayoutOscar.lineCount
                var minLeftOscar = Float.MAX_VALUE
                var maxRightOscar = Float.MIN_VALUE
                var lineIndexOscar = 0

                while (lineIndexOscar < lineCountOscar) {
                    val lineLeftOscar = currentLayoutOscar.getLineLeft(lineIndexOscar)
                    val lineRightOscar = currentLayoutOscar.getLineRight(lineIndexOscar)
                    if (lineLeftOscar < minLeftOscar) {
                        minLeftOscar = lineLeftOscar
                    }
                    if (lineRightOscar > maxRightOscar) {
                        maxRightOscar = lineRightOscar
                    }
                    lineIndexOscar = lineIndexOscar + 1
                }

                if (minLeftOscar < maxRightOscar) {
                    textStartXOscar = minLeftOscar
                    textEndXOscar = maxRightOscar
                }
                textStartYOscar = currentLayoutOscar.getLineTop(0).toFloat()
                textEndYOscar = currentLayoutOscar.getLineBottom(lineCountOscar - 1).toFloat()
            }

            var x0Oscar = 0f
            var y0Oscar = 0f
            var x1Oscar = 0f
            var y1Oscar = 0f

            if (gradientOrientationOscar == ORIENTATION_BOTTOM_TO_TOPOscar) {
                x0Oscar = 0f
                y0Oscar = textEndYOscar
                x1Oscar = 0f
                y1Oscar = textStartYOscar
            } else if (gradientOrientationOscar == ORIENTATION_TOP_TO_BOTTOMOscar) {
                x0Oscar = 0f
                y0Oscar = textStartYOscar
                x1Oscar = 0f
                y1Oscar = textEndYOscar
            } else if (gradientOrientationOscar == ORIENTATION_RIGHT_TO_LEFTOscar) {
                x0Oscar = textEndXOscar
                y0Oscar = 0f
                x1Oscar = textStartXOscar
                y1Oscar = 0f
            } else {
                x0Oscar = textStartXOscar
                y0Oscar = 0f
                x1Oscar = textEndXOscar
                y1Oscar = 0f
            }

            val colorsOscar = intArrayOf(startColorOscar, endColorOscar)
            val newShaderOscar = LinearGradient(
                x0Oscar,
                y0Oscar,
                x1Oscar,
                y1Oscar,
                colorsOscar,
                null,
                Shader.TileMode.CLAMP
            )

            gradientShaderOscar = newShaderOscar
            paint.shader = gradientShaderOscar
        }
    }

    override fun onDetachedFromWindow() {
        paint.shader = null
        gradientShaderOscar = null
        super.onDetachedFromWindow()
    }
}