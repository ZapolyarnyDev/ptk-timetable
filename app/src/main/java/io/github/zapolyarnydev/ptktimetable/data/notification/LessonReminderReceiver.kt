package io.github.zapolyarnydev.ptktimetable.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import io.github.zapolyarnydev.ptktimetable.MainActivity
import io.github.zapolyarnydev.ptktimetable.R

class LessonReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        ensureNotificationChannel(context)

        val noteId = intent.getStringExtra(LessonReminderScheduler.EXTRA_NOTE_ID).orEmpty()
        if (noteId.isBlank()) return

        val title = intent.getStringExtra(LessonReminderScheduler.EXTRA_TITLE)
            ?.takeIf { it.isNotBlank() }
            ?: "Скоро пара"
        val message = intent.getStringExtra(LessonReminderScheduler.EXTRA_MESSAGE)
            ?.takeIf { it.isNotBlank() }
            ?: "Проверьте расписание"

        if (!canPostNotifications(context)) return

        val contentIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(noteId.hashCode(), notification)
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = manager.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Напоминания о парах",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления перед началом занятия"
            }
        )
    }

    companion object {
        const val CHANNEL_ID = "lesson_reminders_channel"
    }
}

