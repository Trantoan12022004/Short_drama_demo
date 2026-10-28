package com.demo.miniflicks.dramaseries.shortstory.util
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.widget.OverScroller
import androidx.core.content.res.ResourcesCompat
import com.demo.miniflicks.dramaseries.shortstory.R
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt


class WheelPickerOscar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var minValue: Int = 0
    var maxValue: Int = 59

    var value: Int = 0
        set(v) {
            field = v.coerceIn(minValue, maxValue)
            scrollOffsetYOscar = (field - minValue) * itemHeightOscar
            invalidate()
        }

    var onValueChanged: ((value: Int) -> Unit)? = null

    // KÍCH THƯỚC CHỮ VÀ MÀU SẮC CHUẨN NHƯ ẢNH
    private val selectedTextSizeOscar = spToPxOscar(28f)       // Cỡ chữ số ở giữa (TO)
    private val unselectedTextSizeOscar = spToPxOscar(16f)     // Cỡ chữ số trên/dưới (NHỎ)
    private val selectedTextColorOscar = Color.WHITE       // Màu trắng sáng số ở giữa
    private val unselectedTextColorOscar = Color.parseColor("#80FFFFFF") // Màu mờ số trên/dưới
    private val dividerColorOscar = Color.parseColor("#40FFFFFF")       // Màu đường gạch ngang Divider

    private var itemHeightOscar = dpToPxOscar(44f)
    private var scrollOffsetYOscar = 0f

    private val selectedPaintOscar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = selectedTextSizeOscar
        color = selectedTextColorOscar
    }

    private val unselectedPaintOscar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = unselectedTextSizeOscar
        color = unselectedTextColorOscar
    }

    private val dividerPaintOscar = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = dividerColorOscar
        strokeWidth = dpToPxOscar(1f)
    }

    private val scrollerOscar = OverScroller(context)
    private var velocityTrackerOscar: VelocityTracker? = null
    private var lastYOscar = 0f

    init {
        try {
            val fontOscar = ResourcesCompat.getFont(context, R.font.plusjakartasans_bold)
            if (fontOscar != null) {
                selectedPaintOscar.typeface = fontOscar
                unselectedPaintOscar.typeface = fontOscar
            }
        } catch (_: Exception) {}
    }

    private fun getItemCountOscar(): Int {
        return maxValue - minValue + 1
    }

    // Đưa index về khoảng 0..(count-1) để vòng chọn chạy liên tục 59 → 00 → 01...
    private fun wrapIndexOscar(indexOscar: Int): Int {
        val countOscar = getItemCountOscar()
        if (countOscar <= 0) {
            return 0
        }

        var wrappedOscar = indexOscar % countOscar
        if (wrappedOscar < 0) {
            wrappedOscar += countOscar
        }
        return wrappedOscar
    }

    // === ĐÃ SỬA KIỂU DỮ LIỆU TẠI ĐÂY: widthMeasureSpec: Int, heightMeasureSpec: Int ===
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthOscar = MeasureSpec.getSize(widthMeasureSpec).coerceAtLeast(dpToPxOscar(50f).toInt())
        val heightOscar = (itemHeightOscar * 3).toInt()
        setMeasuredDimension(widthOscar, heightOscar)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cxOscar = width / 2f
        val centerYOscar = height / 2f

        // Vẽ 2 đường gạch ngang Divider trên và dưới số ở giữa
        val topDividerYOscar = centerYOscar - itemHeightOscar / 2f
        val bottomDividerYOscar = centerYOscar + itemHeightOscar / 2f
        canvas.drawLine(0f, topDividerYOscar, width.toFloat(), topDividerYOscar, dividerPaintOscar)
        canvas.drawLine(0f, bottomDividerYOscar, width.toFloat(), bottomDividerYOscar, dividerPaintOscar)

        // Index tuyệt đối theo offset (có thể âm / vượt max vì cuộn vòng)
        val currentIndexOscar = floor((scrollOffsetYOscar / itemHeightOscar).toDouble()).toInt()

        for (i in (currentIndexOscar - 2)..(currentIndexOscar + 2)) {
            val itemValueOscar = minValue + wrapIndexOscar(i)
            val itemYOscar = centerYOscar + (i * itemHeightOscar - scrollOffsetYOscar)
            val formattedTextOscar = String.format("%02d", itemValueOscar)
            val distanceToCenterOscar = abs(itemYOscar - centerYOscar)

            if (distanceToCenterOscar < itemHeightOscar / 3f) {
                // SỐ Ở GIỮA -> Cỡ chữ TO
                val fontMetricsOscar = selectedPaintOscar.fontMetrics
                val baselineOscar = itemYOscar - (fontMetricsOscar.top + fontMetricsOscar.bottom) / 2f
                canvas.drawText(formattedTextOscar, cxOscar, baselineOscar, selectedPaintOscar)
            } else {
                // SỐ TRÊN/DƯỚI -> Cỡ chữ NHỎ
                val fontMetricsOscar = unselectedPaintOscar.fontMetrics
                val baselineOscar = itemYOscar - (fontMetricsOscar.top + fontMetricsOscar.bottom) / 2f
                canvas.drawText(formattedTextOscar, cxOscar, baselineOscar, unselectedPaintOscar)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (velocityTrackerOscar == null) velocityTrackerOscar = VelocityTracker.obtain()
        velocityTrackerOscar?.addMovement(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!scrollerOscar.isFinished) scrollerOscar.forceFinished(true)
                lastYOscar = event.y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val dyOscar = lastYOscar - event.y
                // Không clamp: cho phép cuộn vượt biên để vòng lại 59 → 00
                scrollOffsetYOscar += dyOscar
                lastYOscar = event.y
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP -> {
                velocityTrackerOscar?.computeCurrentVelocity(1000)
                val vYOscar = velocityTrackerOscar?.yVelocity ?: 0f
                if (abs(vYOscar) > 500) {
                    // Fling không giới hạn biên để quán tính vẫn quay cùng chiều qua 00
                    scrollerOscar.fling(
                        0, scrollOffsetYOscar.toInt(),
                        0, -vYOscar.toInt(),
                        0, 0,
                        Int.MIN_VALUE / 4, Int.MAX_VALUE / 4
                    )
                } else {
                    snapToNearestOscar()
                }
                velocityTrackerOscar?.recycle()
                velocityTrackerOscar = null
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (scrollerOscar.computeScrollOffset()) {
            scrollOffsetYOscar = scrollerOscar.currY.toFloat()
            invalidate()
            if (scrollerOscar.isFinished) {
                snapToNearestOscar()
            }
        }
    }

    private fun snapToNearestOscar() {
        val rawIndexOscar = (scrollOffsetYOscar / itemHeightOscar).roundToInt()
        val wrappedIndexOscar = wrapIndexOscar(rawIndexOscar)
        val newValueOscar = minValue + wrappedIndexOscar

        // Chuẩn hóa offset về khoảng 0..(count-1) để tránh số quá lớn sau nhiều vòng
        scrollOffsetYOscar = wrappedIndexOscar * itemHeightOscar

        if (newValueOscar != value) {
            value = newValueOscar
            onValueChanged?.invoke(value)
        } else {
            invalidate()
        }
    }

    private fun dpToPxOscar(dpOscar: Float): Float = dpOscar * resources.displayMetrics.density
    private fun spToPxOscar(spOscar: Float): Float = spOscar * resources.displayMetrics.scaledDensity
}