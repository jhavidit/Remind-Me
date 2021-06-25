package tech.jhavidit.remindme.service

import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.util.foregroundAndBackgroundLocationPermissionApproved
import tech.jhavidit.remindme.util.showLocationPermissionAlertDialog
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.viewModel.NotesViewModel


class ReminderNotificationService : LifecycleService() {
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val channelId = "default"
        val channelName = "Remind Me"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Remind Me")
                .setContentText("")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(System.currentTimeMillis().toInt(), notification)
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val db: NotesDatabase = NotesDatabase.getDatabase(application)
        val notesDao = db.userDao()
        val alarmReceiver = AlarmReceiver()
        val geoFencingReceiver = GeoFencingReceiver()
        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application)
            .create(NotesViewModel::class.java)
        if (intent?.hasExtra("snooze") == true) {
            val id = intent.getIntExtra("id", -1)
            if (intent.getStringExtra("reminder") == "time") {
                viewModel.selectedNote(id).observe(this, Observer {
                    alarmReceiver.scheduleSnoozeAlarm(applicationContext, it[0], "time")
                    val intentService = Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)
                    val notificationService =
                        Intent(applicationContext, ReminderNotificationService::class.java)
                    applicationContext.stopService(notificationService)
                })
            } else if (intent.getStringExtra("reminder") == "location") {
                viewModel.selectedNote(id).observe(this, Observer {
                    alarmReceiver.scheduleSnoozeAlarm(applicationContext, it[0], "time")
                    val intentService =
                        Intent(applicationContext, LocationReminderService::class.java)
                    applicationContext.stopService(intentService)
                    val notificationService =
                        Intent(applicationContext, ReminderNotificationService::class.java)
                    applicationContext.stopService(notificationService)
                })
            }

        } else if (intent?.hasExtra("dismiss") == true) {
            val id = intent.getIntExtra("id", -1)
            val isSnooze = intent.getBooleanExtra("isSnooze", false)
            if (intent.getStringExtra("reminder") == "time") {
                viewModel.selectedNote(id).observe(this, Observer {
                    val notes = it[0]
                    if (notes.repeatValue == -1L) {
                        val notesModel = NotesModel(
                            id = notes.id,
                            title = notes.title,
                            description = notes.description,
                            locationReminder = notes.locationReminder,
                            timeReminder = null,
                            reminderWaitTime = null,
                            reminderTime = null,
                            reminderDate = null,
                            image = notes.image,
                            isPinned = notes.isPinned,
                            latitude = notes.latitude,
                            longitude = notes.longitude,
                            radius = notes.radius,
                            repeatValue = null,
                            locationName = notes.locationName,
                            backgroundColor = notes.backgroundColor
                        )
                        viewModel.updateNotes(notesModel)
                    }
                    val intentService = Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)
                })
            } else if (intent.getStringExtra("reminder") == "location" && !isSnooze) {
                viewModel.selectedNote(id).observe(this, Observer { note ->
                    val notes = note[0]
                    geoFencingReceiver.cancelLocationReminder(this, notes.id)
                    val notesModel = NotesModel(
                        id = notes.id,
                        title = notes.title,
                        description = notes.description,
                        locationReminder = null,
                        timeReminder = notes.timeReminder,
                        reminderWaitTime = notes.reminderWaitTime,
                        reminderTime = notes.reminderTime,
                        reminderDate = notes.reminderDate,
                        isPinned = notes.isPinned,
                        latitude = null,
                        image = notes.image,
                        longitude = null,
                        radius = null,
                        repeatValue = notes.repeatValue,
                        locationName = null,
                        backgroundColor = notes.backgroundColor
                    )
                    viewModel.updateNotes(notesModel)
                    val intentService =
                        Intent(applicationContext, LocationReminderService::class.java)
                    applicationContext.stopService(intentService)

                })
            } else if (intent.getStringExtra("reminder") == "location" && !isSnooze) {
                viewModel.selectedNote(id).observe(this, Observer { note ->
                    val notes = note[0]
                    val notesModel = NotesModel(
                        id = notes.id,
                        title = notes.title,
                        description = notes.description,
                        locationReminder = null,
                        timeReminder = notes.timeReminder,
                        reminderWaitTime = notes.reminderWaitTime,
                        reminderTime = notes.reminderTime,
                        image = notes.image,
                        reminderDate = notes.reminderDate,
                        isPinned = notes.isPinned,
                        latitude = null,
                        longitude = null,
                        radius = null,
                        repeatValue = notes.repeatValue,
                        locationName = null,
                        backgroundColor = notes.backgroundColor
                    )
                    viewModel.updateNotes(notesModel)
                    val intentService =
                        Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)

                })
            }
            val notificationService =
                Intent(applicationContext, ReminderNotificationService::class.java)
            applicationContext.stopService(notificationService)
        }
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        val intentService = Intent(applicationContext, ReminderNotificationService::class.java)
        applicationContext.stopService(intentService)
    }

}