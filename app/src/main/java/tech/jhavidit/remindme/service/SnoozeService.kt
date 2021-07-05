package tech.jhavidit.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.viewModel.NotesViewModel

class SnoozeService : LifecycleService() {
    private var notes: LiveData<List<NotesModel>>? = null
    private lateinit var notesRepository: NotesRepository
    private lateinit var viewModel: NotesViewModel


    override fun onCreate() {
        super.onCreate()
        viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(
            NotesViewModel::class.java
        )
        val channelId = "default"
        val channelName = "Remind Me"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 21, notificationIntent, 0)
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Remind Me")
                .setContentText("Snoozing Reminder")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
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

        startForeground(20, notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val db: NotesDatabase = NotesDatabase.getDatabase(application)
        val notesDao = db.userDao()
        notesRepository = NotesRepository(notesDao)
        val alarmReceiver = AlarmReceiver()
        val id = intent?.getIntExtra("id", -1)
        val reminder = intent?.getStringExtra("reminder")
        val isSnooze = intent?.getBooleanExtra("isSnooze", false)
        val type = intent?.getStringExtra("type")
        notes = id?.let { notesRepository.selectedNote(it).asLiveData() }
        notes?.observe(this, {
            val notes = it[0]
            if (type != null) {
                alarmReceiver.scheduleSnoozeAlarm(applicationContext, it[0], type)
            }
            toast(applicationContext, "Alarm snoozed for five minutes")
            if (reminder == "time") {
                val intentService = Intent(applicationContext, AlarmService::class.java)
                applicationContext.stopService(intentService)
            } else if (reminder == "location") {
                val intentService =
                    Intent(applicationContext, LocationReminderService::class.java)
                applicationContext.stopService(intentService)
            }
                val notificationService =
                    Intent(applicationContext, SnoozeService::class.java)
                applicationContext.stopService(notificationService)


        })
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        val intentService =
            Intent(applicationContext, SnoozeService::class.java)
        applicationContext.stopService(intentService)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

}