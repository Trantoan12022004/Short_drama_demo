package com.demo.miniflicks.dramaseries.shortstory.ui.widget
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.demo.miniflicks.dramaseries.shortstory.R
import kotlin.math.max
import kotlin.math.min


/**
 * Thanh điều hướng Bottom Bar phiên bản 2 (FluidBulgeBottomNavView)
 * Thiết kế đối xứng trục Y, màu kính mờ #1FFFFFFF, thuật toán biến thiên liên tục triệt tiêu lỗi mép.
 */
class FluidBottomNavViewV2Oscar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint đổ bóng mềm dưới thân thanh bar
    private val shadowPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ thân thanh bar màu kính mờ #1FFFFFFF
    private val barPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ nền nút tròn kính trung tâm màu kính mờ #1FFFFFFF
    private val circleBgPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ viền phản quang kim loại (highlight rim) ở mép trên nút tròn
    private val circleRimPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ hào quang cam phát sáng
    private val glowPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint áp dụng gradient màu cam - đỏ cho icon active
    private val iconGradientPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Path lưu trữ hình học của toàn bộ thân thanh nav
    private val barPathOscar: Path = Path()

    // Danh sách 3 icon: Phim (Movie), Trang chủ (Home), Cài đặt (Settings)
    private val iconDrawablesOscar: Array<Drawable?> = arrayOfNulls(3)

    // Màu sắc theo spec yêu cầu
    private val navGlassColorOscar: Int = "#1FFFFFFF".toColorInt()
    private val inactiveIconColorOscar: Int = "#99FFFFFF".toColorInt()
    private val activeGradientTopColorOscar: Int = "#FFA000".toColorInt()
    private val activeGradientBottomColorOscar: Int = "#FF2800".toColorInt()

    // Quản lý vị trí tab
    var selectedIndex: Int = 1
        private set
    private var previousIndexOscar: Int = 1

    // Tiến trình chuyển động (0f -> 1f) và tọa độ X hiện tại của chỗ phình
    private var animProgressOscar: Float = 1f
    private var currentBulgeXOscar: Float = -1f
    private var animatorOscar: ValueAnimator? = null

    // Callback khi người dùng chuyển tab
    var onTabSelectedListener: ((position: Int) -> Unit)? = null

    init {
        // Tải 3 icon từ drawable
        iconDrawablesOscar[0] = ContextCompat.getDrawable(context, R.drawable.ic_movie)
        iconDrawablesOscar[1] = ContextCompat.getDrawable(context, R.drawable.ic_home)
        iconDrawablesOscar[2] = ContextCompat.getDrawable(context, R.drawable.ic_settings)

        // Cấu hình các đối tượng Paint
        barPaintOscar.style = Paint.Style.FILL
        barPaintOscar.color = navGlassColorOscar

        circleBgPaintOscar.style = Paint.Style.FILL
        circleBgPaintOscar.color = navGlassColorOscar

        glowPaintOscar.style = Paint.Style.FILL

        circleRimPaintOscar.style = Paint.Style.STROKE
        circleRimPaintOscar.isDither = true

        shadowPaintOscar.style = Paint.Style.FILL
        shadowPaintOscar.color = "#20000000".toColorInt()

        iconGradientPaintOscar.isDither = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val tabWidthOscar: Float = w.toFloat() / 3f
        currentBulgeXOscar = selectedIndex * tabWidthOscar + (tabWidthOscar / 2f)
    }

    /**
     * Chuyển tab có hoặc không có animation
     */
    fun selectTab(index: Int, animate: Boolean = true) {
        if (index < 0 || index > 2) {
            return
        }

        if (index == selectedIndex && animProgressOscar >= 1f && width > 0) {
            return
        }

        previousIndexOscar = selectedIndex
        selectedIndex = index

        if (width <= 0) {
            currentBulgeXOscar = -1f
            animProgressOscar = 1f
            onTabSelectedListener?.invoke(index)
            return
        }

        val tabWidthOscar: Float = width.toFloat() / 3f
        val startXOscar: Float
        if (currentBulgeXOscar >= 0f) {
            startXOscar = currentBulgeXOscar
        } else {
            startXOscar = index * tabWidthOscar + (tabWidthOscar / 2f)
        }
        val targetXOscar: Float = index * tabWidthOscar + (tabWidthOscar / 2f)

        if (!animate) {
            currentBulgeXOscar = targetXOscar
            animProgressOscar = 1f
            invalidate()
            onTabSelectedListener?.invoke(index)
            return
        }

        animatorOscar?.cancel()
        val valAnimOscar: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valAnimOscar.duration = 320L
        valAnimOscar.interpolator = AccelerateDecelerateInterpolator()
        valAnimOscar.addUpdateListener { animation ->
            val progressOscar: Float = animation.animatedValue as Float
            animProgressOscar = progressOscar
            currentBulgeXOscar = startXOscar + (targetXOscar - startXOscar) * progressOscar
            invalidate()
        }
        animatorOscar = valAnimOscar
        valAnimOscar.start()

        onTabSelectedListener?.invoke(index)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val wOscar: Float = width.toFloat()
        val hOscar: Float = height.toFloat()
        if (wOscar <= 0f || hOscar <= 0f) {
            return
        }

        val densityOscar: Float = resources.displayMetrics.density

        // 1. Kích thước cơ bản của thanh nav
        val centerYOscar: Float = hOscar / 2f
        val barHeightOscar: Float = 54f * densityOscar
        val pillRadiusOscar: Float = barHeightOscar / 2f
        val topBaseOscar: Float = centerYOscar - pillRadiusOscar
        val bottomBaseOscar: Float = centerYOscar + pillRadiusOscar

        // 2. Kích thước nút tròn và phần phình lượn
        val circleRadiusOscar: Float = 31f * densityOscar
        val bulgeRadiusOscar: Float = circleRadiusOscar + 5f * densityOscar
        val bulgeHalfWidthOscar: Float = 36f * densityOscar

        val cxOscar: Float = currentBulgeXOscar
        val tabWidthOscar: Float = wOscar / 3f
        val cx0Oscar: Float = tabWidthOscar * 0.5f
        val cx1Oscar: Float = tabWidthOscar * 1.5f
        val cx2Oscar: Float = tabWidthOscar * 2.5f

        // Hệ số biến thiên góc bo 2 bên trái và phải (0f: ở giữa -> 1f: ở sát mép)
        val leftMorphOscar: Float
        if (cxOscar < cx1Oscar) {
            val ratioOscar: Float = (cx1Oscar - cxOscar) / (cx1Oscar - cx0Oscar)
            leftMorphOscar = ratioOscar.coerceIn(0f, 1f)
        } else {
            leftMorphOscar = 0f
        }

        val rightMorphOscar: Float
        if (cxOscar > cx1Oscar) {
            val ratioOscar: Float = (cxOscar - cx1Oscar) / (cx2Oscar - cx1Oscar)
            rightMorphOscar = ratioOscar.coerceIn(0f, 1f)
        } else {
            rightMorphOscar = 0f
        }

        // Tọa độ tâm và bán kính bo góc 2 đầu mút biến thiên mượt mà
        val leftCapCenterXOscar: Float = pillRadiusOscar + (cxOscar - pillRadiusOscar) * leftMorphOscar
        val leftCapRadiusOscar: Float = pillRadiusOscar + (bulgeRadiusOscar - pillRadiusOscar) * leftMorphOscar

        val rightCapCenterXOscar: Float = (wOscar - pillRadiusOscar) + (cxOscar - (wOscar - pillRadiusOscar)) * rightMorphOscar
        val rightCapRadiusOscar: Float = pillRadiusOscar + (bulgeRadiusOscar - pillRadiusOscar) * rightMorphOscar

        // Điểm vai chuyển tiếp trên trục X (đảm bảo luôn tăng tiến đơn điệu)
        val startXOscar: Float = max(leftCapCenterXOscar, cxOscar - bulgeHalfWidthOscar)
        val endXOscar: Float = min(rightCapCenterXOscar, cxOscar + bulgeHalfWidthOscar)

        // 3. Xây dựng đường bao hình học barPath liên tục, không bao giờ tự cắt
        barPathOscar.reset()

        // Bắt đầu từ nửa cung tròn đầu mút bên trái (từ 90 độ quét 180 độ lên đỉnh 270 độ)
        val leftCapRectOscar: RectF = RectF(
            leftCapCenterXOscar - leftCapRadiusOscar, centerYOscar - leftCapRadiusOscar,
            leftCapCenterXOscar + leftCapRadiusOscar, centerYOscar + leftCapRadiusOscar
        )
        barPathOscar.arcTo(leftCapRectOscar, 90f, 180f, false)

        // Mép trên: chạy từ trái sang phải
        if (startXOscar > leftCapCenterXOscar) {
            barPathOscar.lineTo(startXOscar, topBaseOscar)
            val ctrl1XOscar: Float = startXOscar + (cxOscar - startXOscar) * 0.45f
            val ctrl1YOscar: Float = topBaseOscar
            val ctrl2XOscar: Float = cxOscar - (cxOscar - startXOscar) * 0.45f
            val ctrl2YOscar: Float = centerYOscar - bulgeRadiusOscar
            barPathOscar.cubicTo(ctrl1XOscar, ctrl1YOscar, ctrl2XOscar, ctrl2YOscar, cxOscar, centerYOscar - bulgeRadiusOscar)
        } else {
            barPathOscar.lineTo(cxOscar, centerYOscar - bulgeRadiusOscar)
        }

        if (endXOscar < rightCapCenterXOscar) {
            val ctrl3XOscar: Float = cxOscar + (endXOscar - cxOscar) * 0.45f
            val ctrl3YOscar: Float = centerYOscar - bulgeRadiusOscar
            val ctrl4XOscar: Float = endXOscar - (endXOscar - cxOscar) * 0.45f
            val ctrl4YOscar: Float = topBaseOscar
            barPathOscar.cubicTo(ctrl3XOscar, ctrl3YOscar, ctrl4XOscar, ctrl4YOscar, endXOscar, topBaseOscar)
            barPathOscar.lineTo(rightCapCenterXOscar, topBaseOscar)
        } else {
            barPathOscar.lineTo(rightCapCenterXOscar, centerYOscar - rightCapRadiusOscar)
        }

        // Nửa cung tròn đầu mút bên phải (từ 270 độ quét 180 độ xuống đáy 90 độ)
        val rightCapRectOscar: RectF = RectF(
            rightCapCenterXOscar - rightCapRadiusOscar, centerYOscar - rightCapRadiusOscar,
            rightCapCenterXOscar + rightCapRadiusOscar, centerYOscar + rightCapRadiusOscar
        )
        barPathOscar.arcTo(rightCapRectOscar, 270f, 180f, false)

        // Mép dưới: chạy ngược từ phải sang trái
        if (endXOscar < rightCapCenterXOscar) {
            barPathOscar.lineTo(endXOscar, bottomBaseOscar)
            val ctrl5XOscar: Float = endXOscar - (endXOscar - cxOscar) * 0.45f
            val ctrl5YOscar: Float = bottomBaseOscar
            val ctrl6XOscar: Float = cxOscar + (endXOscar - cxOscar) * 0.45f
            val ctrl6YOscar: Float = centerYOscar + bulgeRadiusOscar
            barPathOscar.cubicTo(ctrl5XOscar, ctrl5YOscar, ctrl6XOscar, ctrl6YOscar, cxOscar, centerYOscar + bulgeRadiusOscar)
        } else {
            barPathOscar.lineTo(cxOscar, centerYOscar + bulgeRadiusOscar)
        }

        if (startXOscar > leftCapCenterXOscar) {
            val ctrl7XOscar: Float = cxOscar - (cxOscar - startXOscar) * 0.45f
            val ctrl7YOscar: Float = centerYOscar + bulgeRadiusOscar
            val ctrl8XOscar: Float = startXOscar + (cxOscar - startXOscar) * 0.45f
            val ctrl8YOscar: Float = bottomBaseOscar
            barPathOscar.cubicTo(ctrl7XOscar, ctrl7YOscar, ctrl8XOscar, ctrl8YOscar, startXOscar, bottomBaseOscar)
            barPathOscar.lineTo(leftCapCenterXOscar, bottomBaseOscar)
        } else {
            barPathOscar.lineTo(leftCapCenterXOscar, centerYOscar + leftCapRadiusOscar)
        }

        barPathOscar.close()

        // BƯỚC 1: ĐỔ BÓNG MÔI TRƯỜNG DƯỚI TOÀN BỘ THANH NAV
        canvas.save()
        canvas.translate(0f, 3f * densityOscar)
        canvas.drawPath(barPathOscar, shadowPaintOscar)
        canvas.restore()

        // BƯỚC 2: TÔ NỀN KÍNH MỜ #1FFFFFFF CHO THÂN THANH NAV
        canvas.drawPath(barPathOscar, barPaintOscar)

        // BƯỚC 3: TÔ NỀN KÍNH MỜ #1FFFFFFF CHO NÚT TRÒN TRUNG TÂM
        canvas.drawCircle(cxOscar, centerYOscar, circleRadiusOscar, circleBgPaintOscar)

        // BƯỚC 4: VẼ VIỀN PHẢN QUANG (SPECULAR RIM HIGHLIGHT) Ở MÉP TRÊN NÚT TRÒN
        circleRimPaintOscar.strokeWidth = 1.2f * densityOscar
        circleRimPaintOscar.shader = LinearGradient(
            cxOscar, centerYOscar - circleRadiusOscar,
            cxOscar, centerYOscar + circleRadiusOscar,
            intArrayOf(
                Color.argb(160, 255, 255, 255), // Vệt sáng trắng bạc ở vòm trên
                Color.argb(50, 255, 255, 255),
                Color.argb(0, 255, 255, 255)     // Mờ biến mất ở nửa dưới
            ),
            floatArrayOf(0f, 0.35f, 0.75f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cxOscar, centerYOscar, circleRadiusOscar - 0.6f * densityOscar, circleRimPaintOscar)

        // BƯỚC 5: HÀO QUANG CAM PHÁT SÁNG TỪ TÂM NÚT TRÒN
        glowPaintOscar.shader = RadialGradient(
            cxOscar, centerYOscar,
            circleRadiusOscar * 0.9f,
            intArrayOf(
                Color.argb(210, 255, 120, 0), // Lõi cam rực rỡ
                Color.argb(90, 255, 90, 0),   // Vầng sáng hổ phách
                Color.argb(0, 255, 70, 0)     // Mờ dần ra ngoài rìa
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cxOscar, centerYOscar, circleRadiusOscar * 0.9f, glowPaintOscar)

        // BƯỚC 6: VẼ 3 ICON ĐIỀU HƯỚNG
        val normalIconSizeOscar: Float = 22f * densityOscar
        val selectedIconSizeOscar: Float = 32f * densityOscar

        for (i in 0..2) {
            val drawableOscar: Drawable? = iconDrawablesOscar[i]
            if (drawableOscar == null) {
                continue
            }

            val iconXOscar: Float = i * tabWidthOscar + (tabWidthOscar / 2f)
            val iconYOscar: Float = centerYOscar
            val currentIconSizeOscar: Float
            val isCurrentSelectedOscar: Boolean = (i == selectedIndex)

            if (isCurrentSelectedOscar) {
                currentIconSizeOscar = normalIconSizeOscar + (selectedIconSizeOscar - normalIconSizeOscar) * animProgressOscar
            } else if (i == previousIndexOscar && animProgressOscar < 1f) {
                currentIconSizeOscar = selectedIconSizeOscar - (selectedIconSizeOscar - normalIconSizeOscar) * animProgressOscar
            } else {
                currentIconSizeOscar = normalIconSizeOscar
            }

            val halfSizeOscar: Float = currentIconSizeOscar / 2f
            val iconBoundsOscar: Rect = Rect(
                (iconXOscar - halfSizeOscar).toInt(),
                (iconYOscar - halfSizeOscar).toInt(),
                (iconXOscar + halfSizeOscar).toInt(),
                (iconYOscar + halfSizeOscar).toInt()
            )

            if (isCurrentSelectedOscar && animProgressOscar >= 0.8f) {
                // Áp dụng gradient màu Cam - Đỏ cho icon đang chọn
                val iconRectFOscar: RectF = RectF(iconBoundsOscar)
                val saveCountOscar: Int = canvas.saveLayer(iconRectFOscar, null)

                drawableOscar.setTint(Color.WHITE)
                drawableOscar.bounds = iconBoundsOscar
                drawableOscar.draw(canvas)

                iconGradientPaintOscar.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
                iconGradientPaintOscar.shader = LinearGradient(
                    iconRectFOscar.left, iconRectFOscar.top,
                    iconRectFOscar.left, iconRectFOscar.bottom,
                    activeGradientTopColorOscar,
                    activeGradientBottomColorOscar,
                    Shader.TileMode.CLAMP
                )
                canvas.drawRect(iconRectFOscar, iconGradientPaintOscar)
                iconGradientPaintOscar.xfermode = null

                canvas.restoreToCount(saveCountOscar)
            } else {
                // Icon inactive vẽ màu trắng mờ tinh tế trên nền kính
                drawableOscar.setTint(inactiveIconColorOscar)
                drawableOscar.bounds = iconBoundsOscar
                drawableOscar.draw(canvas)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            val tabWidthOscar: Float = width.toFloat() / 3f
            if (tabWidthOscar > 0f) {
                val clickedIndexOscar: Int = (event.x / tabWidthOscar).toInt().coerceIn(0, 2)
                selectTab(clickedIndexOscar, animate = true)
            }
        }
        return true
    }
}