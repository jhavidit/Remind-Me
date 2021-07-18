package tech.jhavidit.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.receiver.DismissReceiver
import tech.jhavidit.remindme.receiver.SnoozeReceiver
import tech.jhavidit.remindme.util.LocalKeyStorage
import tech.jhavidit.remindme.util.NOTES_LOCATION
import tech.jhavidit.remindme.util.stringToUri
import tech.jhavidit.remindme.view.activity.ReminderScreenActivity

class LocationReminderService : Service() {

    private lateinit var vibrator: Vibrator
    private lateinit var ringtone: Ringtone
    private lateinit var uri: Uri
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        LocalKeyStorage(applicationContext).getValue(LocalKeyStorage.RINGTONE)?.let {
            uri = try {
                stringToUri(it)!!
            } catch (e: Exception) {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }

        } ?: run {
            uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        ringtone = RingtoneManager.getRingtone(applicationContext, uri)
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val channelId = "default"
        val channelName = "Remind Me"
        val bundle = intent?.getBundleExtra(NOTES_LOCATION)
        val id = bundle?.getInt("id") ?: 0
        val isSnooze = bundle?.getBoolean("snooze")
        val dismissIntent = Intent(this, DismissReceiver::class.java)
        val snoozeIntent = Intent(this, SnoozeReceiver::class.java)
        snoozeIntent.putExtra("type", "snooze")
        snoozeIntent.putExtra("isSnooze", isSnooze)
        snoozeIntent.putExtra("reminder", "location")
        snoozeIntent.putExtra("id", id)
        dismissIntent.putExtra("isSnooze",isSnooze)
        dismissIntent.putExtra("type", "dismiss")
        dismissIntent.putExtra("reminder", "location")
        dismissIntent.putExtra("id", id)
        val snoozePendingIntent =
            PendingIntent.getBroadcast(this, id+23, snoozeIntent, PendingIntent.FLAG_ONE_SHOT)
        val dismissPendingIntent =
            PendingIntent.getBroadcast(this, id+22, dismissIntent, PendingIntent.FLAG_ONE_SHOT)
        val notificationIntent = Intent(this, ReminderScreenActivity::class.java)
        notificationIntent.putExtra(NOTES_LOCATION, bundle)
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
        bundle?.clear()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone.stop()
        vibrator.cancel()
    }

}

