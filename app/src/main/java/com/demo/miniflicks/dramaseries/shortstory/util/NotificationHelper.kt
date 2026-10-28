package com.demo.miniflicks.dramaseries.shortstory.util
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.demo.miniflicks.dramaseries.shortstory.ui.splash.SplashActivityOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import kotlin.random.Random


object NotificationHelper {
    // Đổi ID để hệ thống tạo Channel mới (vì Channel cũ đã cố định mức DEFAULT)
    private const val CHANNEL_IDOscar = "app_notification_channel_v2"
    private const val CHANNEL_NAMEOscar = "App Updates"

    fun showRandomNotification(context: Context) {
        if (!PermissionManager.checkNotificationPermission(context)) return

        val messagesOscar = context.resources.getStringArray(R.array.random_notifications)
        val randomMessageOscar = messagesOscar[Random.nextInt(messagesOscar.size)]

        val notificationManagerOscar =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelOscar = NotificationChannel(
                CHANNEL_IDOscar,
                CHANNEL_NAMEOscar,
                NotificationManager.IMPORTANCE_HIGH // QUAN TRỌNG: Mức HIGH để hiện Heads-up
            ).apply {
                description = "Channel for high priority notifications"
                enableLights(true)
                enableVibration(true)
            }
            notificationManagerOscar.createNotificationChannel(channelOscar)
        }

        // 1. Tạo Intent để mở SplashActivityOscar khi người dùng nhấn vào thông báo
        val intentOscar = Intent(context, SplashActivityOscar::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntentOscar = PendingIntent.getActivity(
            context,
            0,
            intentOscar,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationOscar = NotificationCompat.Builder(context, CHANNEL_IDOscar)
            .setSmallIcon(R.drawable.ic_noti)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(randomMessageOscar)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // QUAN TRỌNG: Ưu tiên cao
            .setDefaults(NotificationCompat.DEFAULT_ALL)   // Bật âm thanh/rung để kích hoạt Heads-up
            .setAutoCancel(true)
            .setContentIntent(pendingIntentOscar)
            .build()

        notificationManagerOscar.notify(1001, notificationOscar)
    }
}