package com.demo.miniflicks.dramaseries.shortstory.ui.widget
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.ContextCompat
import com.demo.miniflicks.dramaseries.shortstory.R
import androidx.core.graphics.toColorInt
import kotlin.math.atan2
import kotlin.math.sqrt


/**
 * Custom View thanh điều hướng đáy dạng sóng lỏng giả lập kính (Fluid Glass Bottom Navigation)
 * Kích thước chuẩn xác theo Design Spec:
 * - Kích thước nav: rộng 250dp, cao 106dp (thân nav cao 64dp).
 * - Nút tròn nổi: rộng 64dp, cao 64dp (bán kính 32dp), cách lòng máng 5.5dp.
 * - Icon khi select: thu gọn về 34dp (vừa vặn, lộ rõ hào quang bên trong nút tròn 64dp).
 * - Icon khi chưa select: thu gọn về 22dp (thanh thoát, cân đối trong thân nav).
 * - Đã loại bỏ hoàn toàn viền (không viền).
 * - Hiệu ứng chuyển động mượt mà êm ái, tọa độ X cố định không giật.
 */
class FluidBottomNavViewOscar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Paint đổ bóng mềm giả lập khối nổi
    private val shadowPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ thân thanh kính tối (không viền)
    private val barPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint đổ bóng dưới nút tròn nổi
    private val buttonShadowPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ quả cầu kính nổi (không viền)
    private val circlePaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Paint vẽ hào quang vàng phát sáng từ lòng kính
    private val glowPaintOscar: Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // Path lưu trữ hình học thân thanh nav
    private val barPathOscar: Path = Path()

    // 3 Drawable cho 3 Icon: Phim (Cinemix), Trang chủ (Home), Cài đặt (Settings)
    private val iconDrawablesOscar: Array<Drawable?> = arrayOfNulls(3)

    // Đánh giá màu nội suy khi chuyển đổi trạng thái
    private val colorEvaluatorOscar: ArgbEvaluator = ArgbEvaluator()
    private val activeIconColorOscar: Int = "#FFEE00".toColorInt()
    private val inactiveIconColorOscar: Int = "#CCCCCC".toColorInt()

    // Vị trí tab hiện tại và tab trước đó
    var selectedIndex: Int = 0
        private set
    private var previousIndexOscar: Int = 0

    // Tiến trình chuyển động (0f -> 1f) và tọa độ X hiện tại của vết lõm
    private var animProgressOscar: Float = 1f
    private var currentCradleXOscar: Float = -1f
    private var animatorOscar: ValueAnimator? = null

    // Callback thông báo vị trí tab được chọn
    var onTabSelectedListener: ((position: Int) -> Unit)? = null

    init {
        // Tải 3 icon tài nguyên trong drawable
        iconDrawablesOscar[0] = ContextCompat.getDrawable(context, R.drawable.ic_movie)
        iconDrawablesOscar[1] = ContextCompat.getDrawable(context, R.drawable.ic_home)
        iconDrawablesOscar[2] = ContextCompat.getDrawable(context, R.drawable.ic_settings)

        // Cài đặt style dạng FILL thuần túy (không dùng bất kỳ nét viền nào)
        barPaintOscar.style = Paint.Style.FILL
        circlePaintOscar.style = Paint.Style.FILL
        glowPaintOscar.style = Paint.Style.FILL

        shadowPaintOscar.style = Paint.Style.FILL
        shadowPaintOscar.color = "#26000000".toColorInt()

        buttonShadowPaintOscar.style = Paint.Style.FILL
        buttonShadowPaintOscar.color = "#33000000".toColorInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val tabWidthOscar: Float = w.toFloat() / 3f
        currentCradleXOscar = selectedIndex * tabWidthOscar + (tabWidthOscar / 2f)
    }

    /**
     * Chuyển tab với hiệu ứng chuyển động mượt mà
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
            currentCradleXOscar = -1f
            animProgressOscar = 1f
            onTabSelectedListener?.invoke(index)
            return
        }

        val tabWidthOscar: Float = width.toFloat() / 3f
        val startXOscar: Float = if (currentCradleXOscar >= 0f) currentCradleXOscar else (index * tabWidthOscar + (tabWidthOscar / 2f))
        val targetXOscar: Float = index * tabWidthOscar + (tabWidthOscar / 2f)

        if (!animate) {
            currentCradleXOscar = targetXOscar
            animProgressOscar = 1f
            invalidate()
            onTabSelectedListener?.invoke(index)
            return
        }

        animatorOscar?.cancel()
        val valAnimOscar: ValueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valAnimOscar.duration = 350L
        valAnimOscar.interpolator = AccelerateDecelerateInterpolator()
        valAnimOscar.addUpdateListener { animation ->
            animProgressOscar = animation.animatedValue as Float
            currentCradleXOscar = startXOscar + (targetXOscar - startXOscar) * animProgressOscar
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

        // 1. Kích thước riêng phần nav: cao 64dp
        val barHeightOscar: Float = 64f * densityOscar
        val barTopOscar: Float = hOscar - barHeightOscar
        val barBottomOscar: Float = hOscar - 2f * densityOscar
        val defaultPillRadiusOscar: Float = barHeightOscar / 2f

        // 2. Kích thước nút hình tròn: 64dp x 64dp (bán kính 32dp)
        val circleRadiusOscar: Float = 32f * densityOscar
        val circleCenterYOscar: Float = circleRadiusOscar

        // 3. Khoảng hở 5.5dp giữa nút tròn và lòng máng
        val gapOscar: Float = 5.5f * densityOscar
        val cradleArcRadiusOscar: Float = circleRadiusOscar + gapOscar
        val cradleBottomYOscar: Float = circleCenterYOscar + cradleArcRadiusOscar

        // 4. Bán kính cong vai lượn sóng
        val turnRadiusOscar: Float = 14f * densityOscar

        // 5. Tính toán hình học tiếp xúc tiếp tuyến tròn
        val dyOscar: Float = (barTopOscar + turnRadiusOscar) - circleCenterYOscar
        val distCentersOscar: Float = cradleArcRadiusOscar + turnRadiusOscar
        val dxOscar: Float = sqrt(distCentersOscar * distCentersOscar - dyOscar * dyOscar)

        val angleRadOscar: Double = atan2(dyOscar.toDouble(), dxOscar.toDouble())
        val angleDegOscar: Float = Math.toDegrees(angleRadOscar).toFloat()
        val sweepShoulderOscar: Float = 90f - angleDegOscar
        val startCutoutAngleOscar: Float = 180f - angleDegOscar
        val startRightShoulderAngleOscar: Float = 180f + angleDegOscar

        val cxOscar: Float = currentCradleXOscar
        val tabWidthOscar: Float = wOscar / 3f
        val cx0Oscar: Float = tabWidthOscar * 0.5f
        val cx1Oscar: Float = tabWidthOscar * 1.5f
        val cx2Oscar: Float = tabWidthOscar * 2.5f

        // Hệ số biến thiên trạng thái mép trái (0f: Tab giữa/phải -> 1f: Tab trái)
        val leftMorphOscar: Float
        if (cxOscar < cx1Oscar) {
            val ratioOscar: Float = (cx1Oscar - cxOscar) / (cx1Oscar - cx0Oscar)
            leftMorphOscar = ratioOscar.coerceIn(0f, 1f)
        } else {
            leftMorphOscar = 0f
        }

        // Hệ số biến thiên trạng thái mép phải (0f: Tab giữa/trái -> 1f: Tab phải)
        val rightMorphOscar: Float
        if (cxOscar > cx1Oscar) {
            val ratioOscar: Float = (cxOscar - cx1Oscar) / (cx2Oscar - cx1Oscar)
            rightMorphOscar = ratioOscar.coerceIn(0f, 1f)
        } else {
            rightMorphOscar = 0f
        }

        // Tọa độ đỉnh sóng lượn mép trái (chuyển đổi từ bán cầu pill sang đỉnh sóng thấp bo tròn)
        val leftCrestXOscar: Float = defaultPillRadiusOscar - (defaultPillRadiusOscar - 16f * densityOscar) * leftMorphOscar
        val leftCrestYOscar: Float = barTopOscar + (22f * densityOscar) * leftMorphOscar
        val leftCornerRadiusOscar: Float = defaultPillRadiusOscar - (defaultPillRadiusOscar - 14f * densityOscar) * leftMorphOscar

        // Tọa độ đỉnh sóng lượn mép phải
        val rightCrestXOscar: Float = (wOscar - defaultPillRadiusOscar) + (defaultPillRadiusOscar - 16f * densityOscar) * rightMorphOscar
        val rightCrestYOscar: Float = barTopOscar + (22f * densityOscar) * rightMorphOscar
        val rightCornerRadiusOscar: Float = defaultPillRadiusOscar - (defaultPillRadiusOscar - 14f * densityOscar) * rightMorphOscar

        // XÂY DỰNG PATH HÌNH HỌC CHUẨN XÁC CHO CẢ 3 TRẠNG THÁI
        barPathOscar.reset()

        // Bắt đầu từ đỉnh mép trái
        barPathOscar.moveTo(leftCrestXOscar, leftCrestYOscar)

        // 1. VẼ NỬA BÊN TRÁI TIẾN VÀO ĐÁY VẾT LÕM CX
        if (leftMorphOscar > 0.05f) {
            // Khi tab trái được chọn: Uốn lượn từ đỉnh sóng thấp xuống đáy vết lõm ôm quanh quả cầu
            val ctrl1XOscar: Float = leftCrestXOscar + (cxOscar - leftCrestXOscar) * 0.45f
            val ctrl1YOscar: Float = leftCrestYOscar
            val ctrl2XOscar: Float = cxOscar - (cxOscar - leftCrestXOscar) * 0.45f
            val ctrl2YOscar: Float = cradleBottomYOscar

            barPathOscar.cubicTo(ctrl1XOscar, ctrl1YOscar, ctrl2XOscar, ctrl2YOscar, cxOscar, cradleBottomYOscar)
        } else {
            // Khi ở tab giữa hoặc tab phải: Mặt phẳng chạy ngang đến vai lượn sóng
            val leftStartOscar: Float = cxOscar - dxOscar
            barPathOscar.lineTo(leftStartOscar, barTopOscar)

            val leftShoulderRectOscar: RectF = RectF(
                leftStartOscar - turnRadiusOscar, barTopOscar,
                leftStartOscar + turnRadiusOscar, barTopOscar + 2f * turnRadiusOscar
            )
            barPathOscar.arcTo(leftShoulderRectOscar, 270f, sweepShoulderOscar, false)

            val cutoutRectOscar: RectF = RectF(
                cxOscar - cradleArcRadiusOscar, circleCenterYOscar - cradleArcRadiusOscar,
                cxOscar + cradleArcRadiusOscar, circleCenterYOscar + cradleArcRadiusOscar
            )
            barPathOscar.arcTo(cutoutRectOscar, startCutoutAngleOscar, -(90f - angleDegOscar), false)
        }

        // 2. VẼ NỬA TỪ ĐÁY VẾT LÕM CX SANG PHẢI
        if (rightMorphOscar > 0.05f) {
            // Khi tab phải được chọn: Uốn lượn từ đáy vết lõm lên đỉnh sóng bên phải
            val ctrl1XOscar: Float = cxOscar + (rightCrestXOscar - cxOscar) * 0.45f
            val ctrl1YOscar: Float = cradleBottomYOscar
            val ctrl2XOscar: Float = rightCrestXOscar - (rightCrestXOscar - cxOscar) * 0.45f
            val ctrl2YOscar: Float = rightCrestYOscar

            barPathOscar.cubicTo(ctrl1XOscar, ctrl1YOscar, ctrl2XOscar, ctrl2YOscar, rightCrestXOscar, rightCrestYOscar)
        } else {
            // Khi ở tab giữa hoặc tab trái: Nửa cung tròn lòng máng uốn lên mặt phẳng phải
            val cutoutRectOscar: RectF = RectF(
                cxOscar - cradleArcRadiusOscar, circleCenterYOscar - cradleArcRadiusOscar,
                cxOscar + cradleArcRadiusOscar, circleCenterYOscar + cradleArcRadiusOscar
            )
            barPathOscar.arcTo(cutoutRectOscar, 90f, -(90f - angleDegOscar), false)

            val rightEndOscar: Float = cxOscar + dxOscar
            val rightShoulderRectOscar: RectF = RectF(
                rightEndOscar - turnRadiusOscar, barTopOscar,
                rightEndOscar + turnRadiusOscar, barTopOscar + 2f * turnRadiusOscar
            )
            barPathOscar.arcTo(rightShoulderRectOscar, startRightShoulderAngleOscar, sweepShoulderOscar, false)

            barPathOscar.lineTo(rightCrestXOscar, barTopOscar)
        }

        // 3. BO TRÒN MÉP PHẢI XUỐNG ĐÁY
        if (rightMorphOscar > 0.05f) {
            val rightCornerRectOscar: RectF = RectF(
                wOscar - 2f * rightCornerRadiusOscar, rightCrestYOscar,
                wOscar, barBottomOscar
            )
            barPathOscar.arcTo(rightCornerRectOscar, 270f, 180f, false)
        } else {
            val rightArcRectOscar: RectF = RectF(
                wOscar - 2f * defaultPillRadiusOscar, barTopOscar,
                wOscar, barBottomOscar
            )
            barPathOscar.arcTo(rightArcRectOscar, 270f, 180f, false)
        }

        // 4. ĐÁY THANH NAV CHẠY NGANG TỪ PHẢI SANG TRÁI
        barPathOscar.lineTo(leftCrestXOscar, barBottomOscar)

        // 5. BO TRÒN MÉP TRÁI LÊN ĐỈNH
        if (leftMorphOscar > 0.05f) {
            val leftCornerRectOscar: RectF = RectF(
                0f, leftCrestYOscar,
                2f * leftCornerRadiusOscar, barBottomOscar
            )
            barPathOscar.arcTo(leftCornerRectOscar, 90f, 180f, false)
        } else {
            val leftArcRectOscar: RectF = RectF(
                0f, barTopOscar,
                2f * defaultPillRadiusOscar, barBottomOscar
            )
            barPathOscar.arcTo(leftArcRectOscar, 90f, 180f, false)
        }
        barPathOscar.close()

        // BƯỚC 1: ĐỔ BÓNG NỀN MÔI TRƯỜNG DƯỚI THANH NAV
        canvas.save()
        canvas.translate(0f, 2.5f * densityOscar)
        canvas.drawPath(barPathOscar, shadowPaintOscar)
        canvas.restore()

        // BƯỚC 2: TÔ NỀN KÍNH TỐI ĐA TẦNG (KHÔNG DÙNG VIỀN)
        barPaintOscar.shader = LinearGradient(
            0f, barTopOscar,
            0f, barBottomOscar,
            intArrayOf(
                "#4E4E4E".toColorInt(), // Sáng nhẹ tự nhiên ở đỉnh
                "#3D3D3D".toColorInt(), // Màu xám đen kính chủ đạo
                "#2B2B2B".toColorInt()  // Tối dần ở phần đáy
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawPath(barPathOscar, barPaintOscar)

        // BƯỚC 3: ĐỔ BÓNG DƯỚI QUẢ CẦU TRÒN NỔI
        canvas.drawCircle(cxOscar, circleCenterYOscar + 2.5f * densityOscar, circleRadiusOscar, buttonShadowPaintOscar)

        // BƯỚC 4: VẼ QUẢ CẦU KÍNH NỔI (KHÔNG DÙNG VIỀN)
        circlePaintOscar.shader = LinearGradient(
            cxOscar, circleCenterYOscar - circleRadiusOscar,
            cxOscar, circleCenterYOscar + circleRadiusOscar,
            intArrayOf(
                "#5A5A5A".toColorInt(), // Điểm sáng vòm kính cầu
                "#383838".toColorInt(), // Thân kính
                "#242424".toColorInt()  // Đáy kính
            ),
            floatArrayOf(0f, 0.5f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cxOscar, circleCenterYOscar, circleRadiusOscar, circlePaintOscar)

        // BƯỚC 5: HÀO QUANG VÀNG PHÁT SÁNG TỎA TRÒN TỪ TRONG LÒNG KÍNH
        glowPaintOscar.shader = RadialGradient(
            cxOscar, circleCenterYOscar,
            circleRadiusOscar * 0.95f,
            intArrayOf(
                Color.argb(190, 255, 238, 0), // Lõi sáng vàng rực rỡ
                Color.argb(85, 255, 210, 0),  // Quầng sáng hổ phách
                Color.argb(0, 255, 200, 0)    // Mờ dần ra mép kính
            ),
            floatArrayOf(0f, 0.45f, 1f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cxOscar, circleCenterYOscar, circleRadiusOscar * 0.95f, glowPaintOscar)

        // BƯỚC 6: VẼ 3 ICON VỚI KÍCH THƯỚC ĐÃ THU NHỎ CÂN ĐỐI
        val normalIconYOscar: Float = barTopOscar + (barHeightOscar / 2f)
        val elevatedIconYOscar: Float = circleCenterYOscar

        // Kích thước thu nhỏ thanh thoát & cân đối
        val normalIconSizeOscar: Float = 22f * densityOscar
        val selectedIconSizeOscar: Float = 34f * densityOscar

        for (i in 0..2) {
            val drawableOscar: Drawable? = iconDrawablesOscar[i]
            if (drawableOscar == null) {
                continue
            }

            // Tọa độ X cố định ở cột tab của chính icon đó, không bị nhảy giật sang tab khác
            val iconXOscar: Float = i * tabWidthOscar + (tabWidthOscar / 2f)
            val iconYOscar: Float
            val iconColorOscar: Int
            val currentIconSizeOscar: Float

            if (i == selectedIndex) {
                // Icon được chọn: Trồi lên nhẹ nhàng theo trục Y, phóng to lên 34dp và chuyển màu vàng
                iconYOscar = normalIconYOscar + (elevatedIconYOscar - normalIconYOscar) * animProgressOscar
                currentIconSizeOscar = normalIconSizeOscar + (selectedIconSizeOscar - normalIconSizeOscar) * animProgressOscar
                val evaluatedColorOscar: Any? = colorEvaluatorOscar.evaluate(animProgressOscar, inactiveIconColorOscar, activeIconColorOscar)
                if (evaluatedColorOscar is Int) {
                    iconColorOscar = evaluatedColorOscar
                } else {
                    iconColorOscar = activeIconColorOscar
                }
            } else if (i == previousIndexOscar && animProgressOscar < 1f) {
                // Icon vừa bỏ chọn: Hạ từ từ xuống lòng thanh nav, thu nhỏ về 22dp và chuyển màu xám bạc
                iconYOscar = elevatedIconYOscar + (normalIconYOscar - elevatedIconYOscar) * animProgressOscar
                currentIconSizeOscar = selectedIconSizeOscar - (selectedIconSizeOscar - normalIconSizeOscar) * animProgressOscar
                val evaluatedColorOscar: Any? = colorEvaluatorOscar.evaluate(animProgressOscar, activeIconColorOscar, inactiveIconColorOscar)
                if (evaluatedColorOscar is Int) {
                    iconColorOscar = evaluatedColorOscar
                } else {
                    iconColorOscar = inactiveIconColorOscar
                }
            } else {
                // Icon không được chọn: Nằm yên cố định tại vị trí chuẩn trong thanh nav
                iconYOscar = normalIconYOscar
                currentIconSizeOscar = normalIconSizeOscar
                iconColorOscar = inactiveIconColorOscar
            }

            val halfSizeOscar: Float = currentIconSizeOscar / 2f
            drawableOscar.setTint(iconColorOscar)
            drawableOscar.setBounds(
                (iconXOscar - halfSizeOscar).toInt(),
                (iconYOscar - halfSizeOscar).toInt(),
                (iconXOscar + halfSizeOscar).toInt(),
                (iconYOscar + halfSizeOscar).toInt()
            )
            drawableOscar.draw(canvas)
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