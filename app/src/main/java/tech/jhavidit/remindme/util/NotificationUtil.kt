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

fun showNotification(context: Context, title: String) {
    val channelId = "default"
    val channelName = "Remind Me"
    val dismissIntent = Intent(context, TimeReminderActivity::class.java)
    dismissIntent.putExtra("dismiss", "dismiss")
    val dismissPendingIntent = PendingIntent.getActivity(context, 0, dismissIntent, 0)
    val notificationIntent = Intent(context, TimeReminderActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0)
    val alarmTitle =
        String.format("%s", title)
    val notification =
        NotificationCompat.Builder(context, channelId)
            .setContentTitle("We are here to remind you about Location Reminder")
            .setContentText(alarmTitle)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.snooze_icon, "Dismiss", dismissPendingIntent)
            .build()

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)
    }

}

fun notification(context: Context, title: String, pendingIntent: PendingIntent) {


    // checking if android version is greater than oreo(API 26) or not
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
        builder = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
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
            .setSmallIcon(R.drawable.ic_launcher_background)
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
