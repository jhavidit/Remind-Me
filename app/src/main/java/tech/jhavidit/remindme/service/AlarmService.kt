package tech.jhavidit.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.*
import androidx.core.app.NotificationCompat
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.view.activity.TimeReminderActivity

class AlarmService : Service() {
    private lateinit var mediaPlayer : MediaPlayer
    private lateinit var vibrator: Vibrator
    override fun onBind(intent: Intent?): IBinder? {
       return null
    }

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm)
        mediaPlayer.isLooping = true
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val channelId = "default"
        val channelName = "Remind Me"
        val dismissIntent = Intent(this,TimeReminderActivity::class.java)
        dismissIntent.putExtra("dismiss","dismiss")
        val dismissPendingIntent = PendingIntent.getActivity(this,0,dismissIntent,0)
        val notificationIntent = Intent(this, TimeReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val alarmTitle =
            String.format("%s", intent.getStringExtra("title"))
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("We are here to remind you about")
                .setContentText(alarmTitle)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.snooze_icon,"Dismiss",dismissPendingIntent)
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

        mediaPlayer.start()
        val pattern = longArrayOf(0, 100, 1000)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern,0))
        else
            vibrator.vibrate(pattern,0)

        startForeground((1+System.currentTimeMillis()).toInt(), notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        vibrator.cancel()
    }

}