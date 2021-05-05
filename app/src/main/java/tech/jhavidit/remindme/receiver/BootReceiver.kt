package tech.jhavidit.remindme.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import tech.jhavidit.remindme.service.RescheduledAlarmService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            if (Intent.ACTION_BOOT_COMPLETED == intent?.action) {
                val toastText = String.format("Alarm Reboot")
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                startRescheduleAlarmsService(context)
            }
        }
    }

    private fun startRescheduleAlarmsService(context: Context) {
        val intentService = Intent(context, RescheduledAlarmService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intentService)
        } else {
            context.startService(intentService)
        }
    }


}