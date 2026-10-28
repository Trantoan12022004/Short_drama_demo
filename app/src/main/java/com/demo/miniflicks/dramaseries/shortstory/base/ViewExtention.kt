package com.demo.miniflicks.dramaseries.shortstory.base
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.graphics.drawable.Drawable
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.demo.miniflicks.dramaseries.shortstory.data.listener.TapNoHandleListenerOscar
import com.demo.miniflicks.dramaseries.shortstory.R


@SuppressLint("ClickableViewAccessibility")
fun View.tap(action: (view: View) -> Unit) {

    setOnTouchListener { v, event ->

        when (event.action) {

            MotionEvent.ACTION_DOWN -> {

                v.animate()
                    .scaleX(0.90f)
                    .scaleY(0.90f)
                    .alpha(0.7f)
                    .translationY(4f)
                    .setDuration(120)
                    .setInterpolator(
                        DecelerateInterpolator()
                    )
                    .start()
            }


            MotionEvent.ACTION_UP -> {

                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(150)
                    .setInterpolator(
                        OvershootInterpolator()
                    )
                    .start()

                action(v)
            }


            MotionEvent.ACTION_CANCEL -> {

                v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(150)
                    .start()
            }
        }

        true
    }
}


fun View.tapNoHandle(action: (view: View?) -> Unit) {
    // Khởi tạo listener xử lý sự kiện click không kèm hiệu ứng thu phóng
    val listener = object : TapNoHandleListenerOscar() {
        override fun onTapOscar(vOscar: View?) {
            // Thực thi callback truyền vào khi người dùng nhấn vào View
            action(vOscar)
        }
    }
    // Gán listener cho View
    setOnClickListener(listener)
}

@SuppressLint("ClickableViewAccessibility")
fun View.tapDouble(onDoubleClick: () -> Unit) {
    val gestureDetectorOscar = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            onDoubleClick()
            return true
        }
    })

    setOnTouchListener { _, event ->
        gestureDetectorOscar.onTouchEvent(event)
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.inVisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun setSrcImage(imageView: ImageView, drawable: Int) {
    imageView.setImageResource(drawable)
}

fun setTextBackground(context: Context, textView: TextView, drawable: Int) {
    textView.background =
        ContextCompat.getDrawable(context, drawable)
}

fun setTextColor(textView: TextView, color: String) {
    textView.setTextColor(Color.parseColor(color))
}

fun setTintBackgroundView(view: View, color: String) {
    view.backgroundTintList = ColorStateList.valueOf(Color.parseColor(color))
}

/**
 * Tạo CircularProgressDrawable loading spinner dùng làm placeholder cho Glide
 */
fun Context.createProgressDrawable(): CircularProgressDrawable {
    val progressDrawableOscar = CircularProgressDrawable(this)
    progressDrawableOscar.strokeWidth = 5f
    progressDrawableOscar.centerRadius = 30f
    val spinnerColorOscar = ContextCompat.getColor(this, R.color.white)
    progressDrawableOscar.setColorSchemeColors(spinnerColorOscar)
    progressDrawableOscar.start()
    return progressDrawableOscar
}

/**
 * Load ảnh qua Glide kèm ProgressBar xoay tròn loading độc lập,
 * khắc phục việc RoundedImageView đóng băng Drawable animation.
 */
fun ImageView.loadImageWithProgress(
    url: String?,
    progressBar: ProgressBar?,
    errorResId: Int = R.drawable.card_demo
) {
    if (!url.isNullOrEmpty()) {
        if (progressBar != null) {
            progressBar.visibility = View.VISIBLE
        }
        Glide.with(context)
            .load(url)
            .error(errorResId)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (progressBar != null) {
                        progressBar.visibility = View.GONE
                    }
                    return false
                }
            })
            .into(this)
    } else {
        if (progressBar != null) {
            progressBar.visibility = View.GONE
        }
        setImageResource(errorResId)
    }
}