package com.demo.miniflicks.dramaseries.shortstory.util
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat


object PermissionManager {

    /**
     * Kiểm tra một quyền đã được cấp chưa.
     */
    fun Context.isGranted(permission: String): Boolean {
        val resultOscar = ContextCompat.checkSelfPermission(this, permission)
        val grantedOscar = resultOscar == PackageManager.PERMISSION_GRANTED
        return grantedOscar
    }

    /**
     * Kiểm tra quyền Thông báo.
     */
    fun checkNotificationPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val grantedOscar = context.isGranted(Manifest.permission.POST_NOTIFICATIONS)
            return grantedOscar
        } else {
            val notificationManagerOscar = NotificationManagerCompat.from(context)
            val enabledOscar = notificationManagerOscar.areNotificationsEnabled()
            return enabledOscar
        }
    }

    /**
     * Kiểm tra quyền Đọc bộ nhớ / Nhạc (Read Storage / READ_MEDIA_AUDIO).
     */
    fun checkReadPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val grantedOscar = context.isGranted(Manifest.permission.READ_MEDIA_AUDIO)
            return grantedOscar
        } else {
            val grantedOscar = context.isGranted(Manifest.permission.READ_EXTERNAL_STORAGE)
            return grantedOscar
        }
    }

    /**
     * Kiểm tra quyền Ghi cài đặt hệ thống (WRITE_SETTINGS) — dùng để đặt nhạc chuông.
     */
    fun checkWritePermission(context: Context): Boolean {
        return Settings.System.canWrite(context)
    }
}