package tech.jhavidit.remindme.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.activity.MainActivity
import tech.jhavidit.remindme.view.activity.ReminderScreenActivity
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        context?.let {
            intent?.let { it1 -> startAlarmService(context, it1) }
        }
    }

    private fun startAlarmService(
        context: Context,
        intent: Intent
    ) {
        if (LocalKeyStorage(context).getValue(LocalKeyStorage.DO_NOT_DISTURB) == "true") {
            intent.getBundleExtra(NOTES_TIME)?.let { notification(context,"We are here to remind you about", it) }
        } else {

            val intentService = Intent(context, AlarmService::class.java)
            intentService.putExtra(
                NOTES_TIME, intent.getBundleExtra(NOTES_TIME)
            )


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intentService)
            } else {
                context.startService(intentService)
            }
        }
    }


    fun scheduleAlarm(context: Context, notes: NotesModel) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val alarmTime = notes.reminderWaitTime ?: 0L
        val currentTime = Calendar.getInstance().timeInMillis
        if (currentTime < alarmTime) {
            log("notes $notes")
            val intent = Intent(context, AlarmReceiver::class.java)
            val bundle = bundleOf(
                "id" to notes.id,
                "title" to notes.title,
                "description" to notes.description,
                "snooze" to false,
                "reminderTime" to notes.reminderTime,
                "reminder" to "time",
                "locationName" to notes.locationName
            )
            intent.putExtra(NOTES_TIME, bundle)
            val pendingIntent =
                PendingIntent.getBroadcast(
                    context,
                    notes.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            when (notes.repeatValue) {
                -1L -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        pendingIntent
                    )
                }
                else -> {
                    alarmManager.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        alarmTime,
                        notes.repeatValue!!,
                        pendingIntent
                    )
                }

            }
            Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show()
        }

    }

    fun scheduleSnoozeAlarm(context: Context, notes: NotesModel, reminder: String) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val currentTime = Calendar.getInstance().timeInMillis
        val alarmTime = currentTime + fiveMinutes
        val intent = Intent(context, AlarmReceiver::class.java)
        val bundle = bundleOf(
            "id" to notes.id,
            "title" to notes.title,
            "description" to notes.description,
            "reminderTime" to notes.reminderTime,
            "snooze" to true,
            "reminder" to reminder,
            "locationName" to notes.locationName
        )
        intent.putExtra(NOTES_TIME, bundle)
        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                SNOOZE_ID,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                pendingIntent
            )
        }

    }


    fun cancelAlarm(context: Context, id: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent =
            PendingIntent.getBroadcast(context, id, intent, 0)
        alarmManager.cancel(pendingIntent)
        Toast.makeText(context, "Alarm Cancelled", Toast.LENGTH_SHORT).show()
    }
}