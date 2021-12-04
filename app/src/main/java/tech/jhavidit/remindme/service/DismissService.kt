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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.util.LocalKeyStorage
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.viewModel.NotesViewModel

class DismissService : LifecycleService() {
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
                .setContentText("Dismissing Reminder")
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

        startForeground(23, notification)
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
        val geoFencingReceiver = GeoFencingReceiver()
        notes = id?.let { notesRepository.selectedNote(it).asLiveData() }
        notes?.observe(this, Observer {
            val notes = it[0]
            if (reminder == "time") {
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
                        lastUpdated = notes.lastUpdated,
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
            } else if (reminder == "location" && isSnooze == false) {

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
                    lastUpdated = notes.lastUpdated,
                    radius = null,
                    repeatValue = notes.repeatValue,
                    locationName = null,
                    backgroundColor = notes.backgroundColor
                )
                viewModel.updateNotes(notesModel)
                val intentService =
                    Intent(applicationContext, LocationReminderService::class.java)
                applicationContext.stopService(intentService)


            } else if (reminder == "location" && isSnooze == true) {

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
                    lastUpdated = notes.lastUpdated,
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

            }

            deleteKey()
        val notificationService =
            Intent(applicationContext, DismissService::class.java)
        applicationContext.stopService(notificationService)
        })
        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        val intentService = Intent(applicationContext, DismissService::class.java)
        applicationContext.stopService(intentService)
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    private fun deleteKey() {
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.SNOOZE)
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.REMINDER)
        LocalKeyStorage(this).deleteValue(LocalKeyStorage.ID)
    }

}