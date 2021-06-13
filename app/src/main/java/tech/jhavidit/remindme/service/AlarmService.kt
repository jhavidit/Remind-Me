package tech.jhavidit.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.*
import androidx.core.app.NotificationCompat
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.NOTES_TIME
import tech.jhavidit.remindme.view.activity.ReminderScreenActivity


class AlarmService : Service() {

    private lateinit var vibrator: Vibrator
    private lateinit var ringtone: Ringtone
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val channelId = "default"
        val channelName = "Remind Me"
        val bundle = intent.getBundleExtra(NOTES_TIME)
        val id: Int = bundle?.getInt("id") ?: 0
        val dismissIntent = Intent(this, ReminderScreenActivity::class.java)
        val snoozeIntent = Intent(this, ReminderScreenActivity::class.java)
        snoozeIntent.putExtra("snooze", true)
        dismissIntent.putExtra("dismiss", true)
        val snoozePendingIntent = PendingIntent.getActivity(this, id, snoozeIntent, 0)
        val dismissPendingIntent = PendingIntent.getActivity(this, id, dismissIntent, 0)
        val notificationIntent = Intent(this, ReminderScreenActivity::class.java)
        notificationIntent.putExtra(NOTES_TIME, bundle)
        val pendingIntent = PendingIntent.getActivity(
            this,
            id,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmTitle =
            String.format(
                "%s",
                bundle?.getString("title") + "\n" + bundle?.getString("description")
            )
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("We are here to remind you about")
                .setContentText(alarmTitle)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.snooze_icon, "Dismiss", dismissPendingIntent)
                .addAction(R.drawable.snooze_icon, "Snooze", snoozePendingIntent)
                .build()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        ringtone.play()
        val pattern = longArrayOf(0, 100, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        else
            vibrator.vibrate(pattern, 0)

        startForeground((1 + System.currentTimeMillis()).toInt(), notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone.stop()
        vibrator.cancel()
    }

}