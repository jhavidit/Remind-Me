package tech.jhavidit.remindme.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.view.activity.TimeReminderActivity

lateinit var notificationManager: NotificationManager
lateinit var notificationChannel: NotificationChannel
lateinit var builder: Notification.Builder
private val channelId = "default"
private val channelName = "Remind Me"

fun notification(context: Context, title: String, pendingIntent: PendingIntent) {



    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    // checking if android version is greater than oreo(API 26) or not
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
        builder = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.notification_icon
                )
            )
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
    } else {

        builder = Notification.Builder(context)
            .setSmallIcon(R.drawable.notification_icon)
            .setLargeIcon(
                BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.notification_icon
                )
            )
            .setContentTitle(title)
            .setContentIntent(pendingIntent)
    }
    notificationManager.notify((1234 + System.currentTimeMillis()).toInt(), builder.build())
}
