package tech.jhavidit.remindme.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.view.activity.MainActivity

class ReminderNotificationService : LifecycleService() {
    private var notes: LiveData<List<NotesModel>>? = null
    private lateinit var notesRepository: NotesRepository
    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val channelId = "default"
        val channelName = "Remind Me"
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 21, notificationIntent, 0)
        val notification =
            NotificationCompat.Builder(this, channelId)
                .setContentTitle("Remind Me")
                .setContentText("Dismissing/Snoozing Reminder")
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

        if (type == "snooze") {
            notes?.observeForever {
                if (intent.getStringExtra("reminder") == "time") {
                    alarmReceiver.scheduleSnoozeAlarm(applicationContext, it[0], "time")
                    val intentService = Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)
                    val notificationService =
                        Intent(applicationContext, ReminderNotificationService::class.java)
                    applicationContext.stopService(notificationService)
                } else if (intent.getStringExtra("reminder") == "location") {
                    alarmReceiver.scheduleSnoozeAlarm(applicationContext, it[0], "time")
                    val intentService =
                        Intent(applicationContext, LocationReminderService::class.java)
                    applicationContext.stopService(intentService)
                    val notificationService =
                        Intent(applicationContext, ReminderNotificationService::class.java)
                    applicationContext.stopService(notificationService)

                }
            }


        } else if (type == "dismiss") {

            notes?.observeForever {
                val notes = it[0]
                if (intent.getStringExtra("reminder") == "time") {
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
                        lifecycleScope.launch {
                            notesRepository.updateNotes(notesModel)
                        }
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
                        radius = null,
                        repeatValue = notes.repeatValue,
                        locationName = null,
                        backgroundColor = notes.backgroundColor
                    )
                    lifecycleScope.launch {
                        notesRepository.updateNotes(notesModel)
                    }
                    val intentService =
                        Intent(applicationContext, LocationReminderService::class.java)
                    applicationContext.stopService(intentService)


                } else if (type == "location" && isSnooze == true) {

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
                    lifecycleScope.launch {
                        notesRepository.updateNotes(notesModel)
                    }
                    val intentService =
                        Intent(applicationContext, AlarmService::class.java)
                    applicationContext.stopService(intentService)


                }
            }
        }
        notesRepository.readAllData.removeObservers(this)
        notes?.removeObservers(this)
        val notificationService =
            Intent(applicationContext, ReminderNotificationService::class.java)
        applicationContext.stopService(notificationService)



        return START_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()
        notes?.removeObservers(this)
        notesRepository.readAllData.removeObservers(this)
        val intentService = Intent(applicationContext, ReminderNotificationService::class.java)
        applicationContext.stopService(intentService)
    }

}