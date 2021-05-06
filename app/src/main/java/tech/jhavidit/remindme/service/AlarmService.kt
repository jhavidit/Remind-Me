package tech.jhavidit.remindme.service

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import android.os.Vibrator
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
        val notificationIntent = Intent(this, TimeReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val alarmTitle =
            String.format("%s Alarm", intent.getStringExtra("title"))
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle(alarmTitle)
                .setContentText("Ring Ring .. Ring Ring")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .build()
        mediaPlayer.start()
        val pattern = longArrayOf(0, 100, 1000)
        vibrator.vibrate(pattern, 0)
        startForeground(1, notification)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        vibrator.cancel()
    }

}