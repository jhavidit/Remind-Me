package tech.jhavidit.remindme.service

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase

class RescheduledAlarmService : LifecycleService() {

//    private val db: NotesDatabase = NotesDatabase.getDatabase(application)
//    private val notesDao = db.userDao()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
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
        })

        return START_STICKY

    }


    override fun onBind(intent: Intent): IBinder? {
         super.onBind(intent)
        return null
    }
}