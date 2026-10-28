package com.demo.miniflicks.dramaseries.shortstory.ui.widget
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import com.demo.miniflicks.dramaseries.shortstory.R


class DiagonalImageViewOscar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    // Hướng dốc vát chéo
    enum class SlopeDirectionOscar {
        DOWN_RIGHT, // Trái cao, phải thấp (\)
        DOWN_LEFT   // Trái thấp, phải cao (/)
    }

    // Đường path dùng để cắt ảnh và vẽ viền
    private val clipPathOscar = Path()

    // Bán kính bo tròn các góc (16dp)
    private val cornerRadiusOscar: Float = 16f * resources.displayMetrics.density

    // Độ chênh lệch chiều cao vát chéo (20dp)
    private val diagonalHeightOscar: Float = 20f * resources.displayMetrics.density

    // Bề dày viền highlight (2.5dp)
    private val highlightStrokeWidthOscar: Float = 2.5f * resources.displayMetrics.density

    // Hướng cắt chéo hiện tại (mặc định dốc xuống bên phải)
    var slopeDirection: SlopeDirectionOscar = SlopeDirectionOscar.DOWN_RIGHT
        set(value) {
            field = value
            updatePathOscar(width, height)
            invalidate()
        }

    // Trạng thái bật/tắt viền vàng highlight
    var isHighlighted: Boolean = false
        set(value) {
            field = value
            updatePathOscar(width, height)
            invalidate()
        }

    // Paint vẽ viền vàng highlight
    private val borderPaintOscar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = highlightStrokeWidthOscar
        color = ContextCompat.getColor(context, R.color.Orange_40)
    }

    // Paint vẽ gradient tối ở chân poster
    private val gradientPaintOscar = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePathOscar(w, h)
        updateGradientOscar(w, h)
    }

    // Cập nhật shader cho gradient tối ở đáy
    private fun updateGradientOscar(wOscar: Int, hOscar: Int) {
        if (wOscar <= 0 || hOscar <= 0) {
            return
        }
        val gradientHeightOscar: Float = 45f * resources.displayMetrics.density
        val startYOscar: Float = hOscar.toFloat() - gradientHeightOscar
        val endYOscar: Float = hOscar.toFloat()

        gradientPaintOscar.shader = LinearGradient(
            0f,
            startYOscar,
            0f,
            endYOscar,
            Color.TRANSPARENT,
            Color.argb(200, 0, 0, 0),
            Shader.TileMode.CLAMP
        )
    }

    // Xây dựng đường Path cắt chéo với các góc bo tròn tiếp tuyến mượt mà
    private fun updatePathOscar(wOscar: Int, hOscar: Int) {
        if (wOscar <= 0 || hOscar <= 0) {
            return
        }

        clipPathOscar.reset()

        // Tính toán khoảng thụt vào (inset) nếu có viền để viền không bị cắt ở mép view
        val insetOscar: Float
        if (isHighlighted) {
            insetOscar = highlightStrokeWidthOscar / 2f
        } else {
            insetOscar = 0f
        }

        val leftOscar: Float = insetOscar
        val topOscar: Float = insetOscar
        val rightOscar: Float = wOscar.toFloat() - insetOscar
        val bottomOscar: Float = hOscar.toFloat() - insetOscar

        val cardWidthOscar: Float = rightOscar - leftOscar
        val rOscar: Float = cornerRadiusOscar
        val sOscar: Float = diagonalHeightOscar

        if (slopeDirection == SlopeDirectionOscar.DOWN_RIGHT) {
            // Trường hợp 1: Trái cao, phải thấp (\)
            // Điểm bắt đầu ở cạnh trái ngay dưới góc bo trên-trái
            clipPathOscar.moveTo(leftOscar, topOscar + rOscar)

            // Góc trên-trái: uốn cong nối tiếp tuyến vào đường dốc
            val endTopLeftYOscar: Float = topOscar + (sOscar / cardWidthOscar) * rOscar
            clipPathOscar.quadTo(leftOscar, topOscar, leftOscar + rOscar, endTopLeftYOscar)

            // Cạnh trên dốc xuống bên phải
            val startTopRightYOscar: Float = topOscar + sOscar - (sOscar / cardWidthOscar) * rOscar
            clipPathOscar.lineTo(rightOscar - rOscar, startTopRightYOscar)

            // Góc trên-phải: uốn cong nối vào cạnh đứng bên phải
            clipPathOscar.quadTo(rightOscar, topOscar + sOscar, rightOscar, topOscar + sOscar + rOscar)

            // Cạnh phải đi thẳng xuống
            clipPathOscar.lineTo(rightOscar, bottomOscar - rOscar)

            // Góc dưới-phải
            clipPathOscar.quadTo(rightOscar, bottomOscar, rightOscar - rOscar, bottomOscar)

            // Cạnh đáy nằm ngang
            clipPathOscar.lineTo(leftOscar + rOscar, bottomOscar)

            // Góc dưới-trái
            clipPathOscar.quadTo(leftOscar, bottomOscar, leftOscar, bottomOscar - rOscar)

            clipPathOscar.close()
        } else {
            // Trường hợp 2: Trái thấp, phải cao (/)
            // Điểm bắt đầu ở cạnh trái ngay dưới góc bo trên-trái
            clipPathOscar.moveTo(leftOscar, topOscar + sOscar + rOscar)

            // Góc trên-trái: uốn cong nối tiếp tuyến vào đường dốc
            val endTopLeftYOscar: Float = topOscar + sOscar - (sOscar / cardWidthOscar) * rOscar
            clipPathOscar.quadTo(leftOscar, topOscar + sOscar, leftOscar + rOscar, endTopLeftYOscar)

            // Cạnh trên dốc lên bên phải
            val startTopRightYOscar: Float = topOscar + (sOscar / cardWidthOscar) * rOscar
            clipPathOscar.lineTo(rightOscar - rOscar, startTopRightYOscar)

            // Góc trên-phải: uốn cong nối vào cạnh đứng bên phải
            clipPathOscar.quadTo(rightOscar, topOscar, rightOscar, topOscar + rOscar)

            // Cạnh phải đi thẳng xuống
            clipPathOscar.lineTo(rightOscar, bottomOscar - rOscar)

            // Góc dưới-phải
            clipPathOscar.quadTo(rightOscar, bottomOscar, rightOscar - rOscar, bottomOscar)

            // Cạnh đáy nằm ngang
            clipPathOscar.lineTo(leftOscar + rOscar, bottomOscar)

            // Góc dưới-trái
            clipPathOscar.quadTo(leftOscar, bottomOscar, leftOscar, bottomOscar - rOscar)

            clipPathOscar.close()
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (width <= 0 || height <= 0) {
            return
        }

        // 1. Cắt canvas theo đường Path
        val saveCountOscar: Int = canvas.save()
        canvas.clipPath(clipPathOscar)

        // 2. Vẽ ảnh poster gốc
        super.onDraw(canvas)

        // 3. Vẽ dải gradient tối ở đáy (nằm trong vùng cắt)
        val gradientHeightOscar: Float = 45f * resources.displayMetrics.density
        canvas.drawRect(
            0f,
            height.toFloat() - gradientHeightOscar,
            width.toFloat(),
            height.toFloat(),
            gradientPaintOscar
        )

        canvas.restoreToCount(saveCountOscar)

        // 4. Nếu bật viền highlight thì vẽ viền ôm sát chu vi poster
        if (isHighlighted) {
            canvas.drawPath(clipPathOscar, borderPaintOscar)
        }
    }
}