package tech.jhavidit.remindme.util

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.view.activity.ReminderScreenActivity

lateinit var notificationManager: NotificationManager
lateinit var notificationChannel: NotificationChannel
lateinit var builder: Notification.Builder
private const val channelId = "default"
private const val channelName = "Remind Me"

fun notification(context: Context,title:String, bundle: Bundle) {

    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    // checking if android version is greater than oreo(API 26) or not
    val intentNotification = Intent(context, ReminderScreenActivity::class.java)
    intentNotification.putExtra(NOTES_TIME, bundle)
    val id = bundle.getInt("id", -1)
    val pendingIntent = PendingIntent.getActivity(
        context,
        id,
        intentNotification,
        PendingIntent.FLAG_UPDATE_CURRENT
    )
    val description =
        String.format("%s", bundle.getString("title") + "\n" + bundle.getString("description"))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationChannel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.GREEN
        notificationChannel.enableVibration(false)
        notificationManager.createNotificationChannel(notificationChannel)
        builder = Notification.Builder(context, channelId)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    } else {

        builder = Notification.Builder(context)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle(title)
            .setContentText(description)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }
    notificationManager.notify((1234 + System.currentTimeMillis()).toInt(), builder.build())
}
