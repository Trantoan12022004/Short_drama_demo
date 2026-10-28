package com.demo.miniflicks.dramaseries.shortstory.util
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator


class CurvedBottomBarViewOscar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val fillPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val borderPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val barPathOscar: Path = Path()
    private val topStrokePathOscar: Path = Path()

    private var cradleRadiusOscar: Float = 0f
    private var cornerRadiusOscar: Float = 0f

    // Tọa độ tâm vết lõm hiện tại (có thể di chuyển từ trái sang phải)
    private var cradleCenterXOscar: Float = -1f
    private var cradleAnimatorOscar: ValueAnimator? = null

    init {
        val densityOscar: Float = context.resources.displayMetrics.density
        cradleRadiusOscar = 34f * densityOscar
        cornerRadiusOscar = 28f * densityOscar

        fillPaintOscar.style = Paint.Style.FILL

        strokePaintOscar.style = Paint.Style.STROKE
        strokePaintOscar.strokeWidth = 1.5f * densityOscar

        borderPaintOscar.style = Paint.Style.STROKE
        borderPaintOscar.strokeWidth = 1f * densityOscar
        borderPaintOscar.color = Color.parseColor("#33FFFFFF")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val widthFloatOscar: Float = w.toFloat()
        val heightFloatOscar: Float = h.toFloat()

        // Mặc định ban đầu vết lõm nằm ở tab đầu tiên (1/6 chiều rộng) hoặc giữa
        if (cradleCenterXOscar < 0f) {
            cradleCenterXOscar = widthFloatOscar / 6f
        }

        // Nền đen tím sâu
        fillPaintOscar.shader = LinearGradient(
            0f, 0f, 0f, heightFloatOscar,
            intArrayOf(
                Color.parseColor("#F01B1124"),
                Color.parseColor("#F5100918"),
                Color.parseColor("#F80A040F")
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )

        // Gradient vệt sáng viền trên
        strokePaintOscar.shader = LinearGradient(
            0f, 0f, widthFloatOscar, 0f,
            intArrayOf(
                Color.parseColor("#00FFFFFF"),
                Color.parseColor("#99FFFFFF"),
                Color.parseColor("#D9FFFFFF"),
                Color.parseColor("#99FFFFFF"),
                Color.parseColor("#00FFFFFF")
            ),
            floatArrayOf(0f, 0.2f, 0.5f, 0.8f, 1f),
            Shader.TileMode.CLAMP
        )

        buildBarPathsOscar(widthFloatOscar, heightFloatOscar)
    }

    /**
     * Chạy animation di chuyển vết lõm đến vị trí tâm X mới
     */
    fun animateCradleTo(targetX: Float, duration: Long = 300L) {
        cradleAnimatorOscar?.cancel()
        val startXOscar: Float = cradleCenterXOscar
        val animatorOscar: ValueAnimator = ValueAnimator.ofFloat(startXOscar, targetX)
        animatorOscar.duration = duration
        animatorOscar.interpolator = DecelerateInterpolator()
        animatorOscar.addUpdateListener { animation ->
            val valueOscar: Float = animation.animatedValue as Float
            cradleCenterXOscar = valueOscar
            buildBarPathsOscar(width.toFloat(), height.toFloat())
            invalidate()
        }
        cradleAnimatorOscar = animatorOscar
        animatorOscar.start()
    }

    private fun buildBarPathsOscar(widthOscar: Float, heightOscar: Float) {
        if (widthOscar <= 0f || heightOscar <= 0f) {
            return
        }

        barPathOscar.reset()
        topStrokePathOscar.reset()

        val topYOscar: Float = 0f
        val bottomYOscar: Float = heightOscar
        val pillRadiusOscar: Float = heightOscar / 2f

        val turnRadiusOscar: Float = 12f * context.resources.displayMetrics.density
        val cradleDepthOscar: Float = cradleRadiusOscar * 0.72f

        // Tọa độ bắt đầu và kết thúc của vết lõm
        val leftCradleStartOscar: Float = (cradleCenterXOscar - cradleRadiusOscar - turnRadiusOscar).coerceAtLeast(pillRadiusOscar * 0.3f)
        val rightCradleEndOscar: Float = (cradleCenterXOscar + cradleRadiusOscar + turnRadiusOscar).coerceAtMost(widthOscar - pillRadiusOscar * 0.3f)

        // Bắt đầu từ mép bo tròn trái
        barPathOscar.moveTo(pillRadiusOscar, topYOscar)
        topStrokePathOscar.moveTo(pillRadiusOscar, topYOscar)

        // Đường thẳng đến điểm bắt đầu vết lõm
        if (leftCradleStartOscar > pillRadiusOscar) {
            barPathOscar.lineTo(leftCradleStartOscar, topYOscar)
            topStrokePathOscar.lineTo(leftCradleStartOscar, topYOscar)
        }

        // Uốn lượn xuống đáy vết lõm
        barPathOscar.cubicTo(
            cradleCenterXOscar - cradleRadiusOscar, topYOscar,
            cradleCenterXOscar - turnRadiusOscar * 0.5f, cradleDepthOscar,
            cradleCenterXOscar, cradleDepthOscar
        )
        topStrokePathOscar.cubicTo(
            cradleCenterXOscar - cradleRadiusOscar, topYOscar,
            cradleCenterXOscar - turnRadiusOscar * 0.5f, cradleDepthOscar,
            cradleCenterXOscar, cradleDepthOscar
        )

        // Uốn lượn lên lại mép phải
        barPathOscar.cubicTo(
            cradleCenterXOscar + turnRadiusOscar * 0.5f, cradleDepthOscar,
            cradleCenterXOscar + cradleRadiusOscar, topYOscar,
            rightCradleEndOscar, topYOscar
        )
        topStrokePathOscar.cubicTo(
            cradleCenterXOscar + turnRadiusOscar * 0.5f, cradleDepthOscar,
            cradleCenterXOscar + cradleRadiusOscar, topYOscar,
            rightCradleEndOscar, topYOscar
        )

        // Đường thẳng đến góc bo phải
        if (rightCradleEndOscar < widthOscar - pillRadiusOscar) {
            barPathOscar.lineTo(widthOscar - pillRadiusOscar, topYOscar)
            topStrokePathOscar.lineTo(widthOscar - pillRadiusOscar, topYOscar)
        }

        // Cung tròn bên phải
        val rightArcRectOscar: RectF = RectF(widthOscar - 2 * pillRadiusOscar, topYOscar, widthOscar, bottomYOscar)
        barPathOscar.arcTo(rightArcRectOscar, 270f, 180f, false)

        // Đường đáy
        barPathOscar.lineTo(pillRadiusOscar, bottomYOscar)

        // Cung tròn bên trái
        val leftArcRectOscar: RectF = RectF(0f, topYOscar, 2 * pillRadiusOscar, bottomYOscar)
        barPathOscar.arcTo(leftArcRectOscar, 90f, 180f, false)
        barPathOscar.close()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawPath(barPathOscar, fillPaintOscar)
        canvas.drawPath(barPathOscar, borderPaintOscar)
        canvas.drawPath(topStrokePathOscar, strokePaintOscar)
    }
}