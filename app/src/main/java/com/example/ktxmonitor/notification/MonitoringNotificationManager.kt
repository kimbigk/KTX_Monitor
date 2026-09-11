package com.example.ktxmonitor.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.ktxmonitor.MainActivity
import com.example.ktxmonitor.R
import com.example.ktxmonitor.domain.model.WatchTarget

class MonitoringNotificationManager(private val context: Context) {
    companion object {
        const val SEAT_CHANNEL_ID = "ktx_seat_found_channel"
        const val SERVICE_CHANNEL_ID = "ktx_service_channel"
        const val SERVICE_NOTIFICATION_ID = 1001
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 좌석 발견 고우선순위 채널
            val seatChannel = NotificationChannel(
                SEAT_CHANNEL_ID,
                "KTX 잔여석 발견 알림",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "조건에 맞는 KTX 좌석이 발견되었을 때 즉시 알립니다."
                enableVibration(true)
            }

            // 포그라운드 서비스 상시 표시 채널
            val serviceChannel = NotificationChannel(
                SERVICE_CHANNEL_ID,
                "KTX 감시 서비스 상태",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "백그라운드에서 KTX 좌석을 감시하는 서비스 상태입니다."
            }

            notificationManager.createNotificationChannel(seatChannel)
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    fun notifySeatFound(target: WatchTarget, trainInfo: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            target.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🎉 [KTX 좌석 발견] ${target.departureStation} → ${target.arrivalStation}"
        val content = "${target.date} $trainInfo (${target.seatType}, ${target.passengerCount}명)"

        val korailIntent = context.packageManager.getLaunchIntentForPackage("com.korail.talk")
            ?: Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://m.letskorail.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        val korailPendingIntent = PendingIntent.getActivity(
            context,
            target.id.hashCode() + 1,
            korailIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, SEAT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$content\n지금 바로 예매하세요!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_send,
                "코레일 예매 이동",
                korailPendingIntent
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(target.id.hashCode(), notification)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    fun buildServiceNotification(activeTargetsCount: Int): Notification {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (activeTargetsCount > 0) {
            "현재 ${activeTargetsCount}개의 KTX 일정을 감시 중입니다."
        } else {
            "활성화된 감시 대상이 없습니다."
        }

        return NotificationCompat.Builder(context, SERVICE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("KTX 취소표 감시 실행 중")
            .setContentText(contentText)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
