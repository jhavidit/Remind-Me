package tech.jhavidit.remindme.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.view.activity.TimeReminderActivity

class RescheduledAlarmService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()

        val channelId = "default"
        val channelName = "Remind Me"
        val notificationIntent = Intent(this, TimeReminderActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Remind Me")
                .setContentText("Setting up Time Based Reminder")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(2, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        val intentService = Intent(applicationContext, RescheduledAlarmService::class.java)
        applicationContext.stopService(intentService)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val db : NotesDatabase = NotesDatabase.getDatabase(application)
        val notesDao = db.userDao()
        val notesRepository = NotesRepository(notesDao)
        val alarmReceiver = AlarmReceiver()
        notesRepository.readAllData.observe(this, Observer {
            for (notes in it) {
                alarmReceiver.scheduleAlarm(applicationContext, notes)
            }
            val intentService = Intent(applicationContext, RescheduledAlarmService::class.java)
            applicationContext.stopService(intentService)
        })

        return START_STICKY

    }


    override fun onBind(intent: Intent): IBinder? {
         super.onBind(intent)
        return null
    }
}