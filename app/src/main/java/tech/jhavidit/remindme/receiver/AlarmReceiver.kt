package tech.jhavidit.remindme.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.view.activity.MainActivity
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.let { it1 -> startAlarmService(context, it1) }
        }
    }

    private fun startAlarmService(
        context: Context,
        intent: Intent
    ) {
        val intentService = Intent(context, AlarmService::class.java)
        intentService.putExtra(
            "title",
            intent.getStringExtra("title") ?: ""
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }

    private fun showNotification(context: Context, title: String) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val channelId = "default"
        val channelName = "Remind Me"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setColor(ContextCompat.getColor(context, R.color.purple_200))
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("We are here to remind you about")
            .setContentText(title)
            .setAutoCancel(false)
            .setVibrate(longArrayOf(1000, 500, 1000, 500, 1000, 500))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun scheduleAlarm(context: Context, notes: NotesModel) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmTime = notes.reminderTime ?: 0L
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime < alarmTime) {
            val intent = Intent(context, AlarmReceiver::class.java)
            val title = notes.title + "\n" + notes.description
            intent.putExtra("title", title)
            val pendingIntent =
                PendingIntent.getBroadcast(context, notes.id, intent, 0)
            when (notes.repeatAlarmIndex) {
                0 -> alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
                1 -> alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    alarmTime,
                    AlarmManager.INTERVAL_HOUR,
                    pendingIntent
                )
                2 ->
                    alarmManager.setInexactRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        alarmTime,
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent
                    )
                3 -> alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    alarmTime,
                    AlarmManager.INTERVAL_DAY * 7,
                    pendingIntent
                )
                4 -> alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    alarmTime,
                    AlarmManager.INTERVAL_DAY * 30,
                    pendingIntent
                )
                5 ->
                    alarmManager.setInexactRepeating(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        alarmTime,
                        AlarmManager.INTERVAL_DAY * 365,
                        pendingIntent
                    )
            }
            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show()
        }

    }

    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, 0)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_SHORT).show()
    }
}