package tech.jhavidit.remindme.receiver

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.service.LocationReminderService
import tech.jhavidit.remindme.service.ReminderNotificationService
import tech.jhavidit.remindme.service.RescheduledAlarmService
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.viewModel.NotesViewModel

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
          context?.let {
            intent?.let { it1 -> startNotificationService(context, it1) }
        }
    }

    private fun startNotificationService(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val reminder = intent.getStringExtra("reminder")
        val isSnooze = intent.getBooleanExtra("isSnooze", false)
        val type = intent.getStringExtra("type")
        val intentService = Intent(context, ReminderNotificationService::class.java)
        intentService.putExtra("id", id)
        intentService.putExtra("reminder", reminder)
        intentService.putExtra("isSnooze", isSnooze)
        intentService.putExtra("type", type)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }


}