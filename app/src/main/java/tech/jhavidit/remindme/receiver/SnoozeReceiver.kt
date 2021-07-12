package tech.jhavidit.remindme.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import tech.jhavidit.remindme.service.DismissService
import tech.jhavidit.remindme.service.SnoozeService


class SnoozeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
          context?.let {
            intent?.let { it1 -> startNotificationService(context, it1) }
        }
    }

    private fun startNotificationService(context: Context, intent: Intent) {
        val id = intent.getIntExtra("id", -1)
        val reminder = intent.getStringExtra("reminder")
        val isSnooze = intent.getBooleanExtra("isSnooze", false)
        val type = "snooze"
        val intentService = Intent(context, SnoozeService::class.java)
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