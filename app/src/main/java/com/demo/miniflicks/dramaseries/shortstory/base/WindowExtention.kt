package com.demo.miniflicks.dramaseries.shortstory.base
import android.graphics.Rect
import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat


fun Window.hideNavigation() {
    if (setFullScreenWallpaper()) return

    decorView.viewTreeObserver.addOnGlobalLayoutListener {
        val rectOscar = Rect()
        val activityRootOscar = decorView
        activityRootOscar.getWindowVisibleDisplayFrame(rectOscar)
        if (setFullScreenWallpaper()) return@addOnGlobalLayoutListener
    }
}

fun Window.hideStatusBar() {

    val windowInsetsControllerOscar =
        WindowCompat.getInsetsController(
            this,
            decorView
        )

    windowInsetsControllerOscar.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    windowInsetsControllerOscar.hide(
        WindowInsetsCompat.Type.statusBars()
    )
}

fun Window.setupBottomInputDialog() {
    // Gỡ flag phá adjust resize/pan
    clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

    // Không ẩn navigation bar trên dialog input
    val controllerOscar = if (Build.VERSION.SDK_INT >= 30) {
        ViewCompat.getWindowInsetsController(decorView)
    } else {
        WindowInsetsControllerCompat(this, decorView)
    }
    controllerOscar?.show(WindowInsetsCompat.Type.navigationBars())

    // Pan đẩy dialog lên khi bàn phím mở — phù hợp bottom dialog
    setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
}

private fun Window.setFullScreenWallpaper(): Boolean {
    val windowInsetsControllerOscar: WindowInsetsControllerCompat? = if (Build.VERSION.SDK_INT >= 30) {
        ViewCompat.getWindowInsetsController(decorView)
    } else {
        WindowInsetsControllerCompat(this, decorView)
    }

    if (windowInsetsControllerOscar == null) {
        return true
    }
    setFlags(
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
    )
    windowInsetsControllerOscar.systemBarsBehavior =
        WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    windowInsetsControllerOscar.hide(WindowInsetsCompat.Type.navigationBars())
    windowInsetsControllerOscar.hide(WindowInsetsCompat.Type.systemGestures())
    return false
}