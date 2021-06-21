package tech.jhavidit.remindme.view.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_create_notes.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.service.LocationReminderService
import tech.jhavidit.remindme.util.NOTES_LOCATION
import tech.jhavidit.remindme.util.NOTES_TIME
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.viewModel.NotesViewModel

class ReminderScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var alarmReceiver: AlarmReceiver
    private var id: Int = 0
    private var snooze: Boolean = false
    private var reminder: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_reminder)
        alarmReceiver = AlarmReceiver()
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val notesTimeBundle = intent?.getBundleExtra(NOTES_TIME)
        val snoozeAlarm = intent?.getBooleanExtra("snooze", false)
        val dismissAlarm = intent?.getBooleanExtra("dismiss", false)


        notesTimeBundle?.let {
            id = notesTimeBundle.getInt("id")
            viewModel.selectedNote(id).observe(this, Observer {
                binding.reminderLocationTime.text =
                    it[0].reminderTime
                binding.title.text = it[0].title
                binding.description.text = it[0].description
            })
            snooze = notesTimeBundle.getBoolean("snooze")
            reminder = notesTimeBundle.getString("reminder") ?: ""
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
        }


        val notesLocationBundle = intent?.getBundleExtra(NOTES_LOCATION)

        notesLocationBundle?.let {
            id = notesLocationBundle.getInt("id")
            snooze = notesLocationBundle.getBoolean("snooze")
            reminder = notesLocationBundle.getString("reminder") ?: ""
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            viewModel.selectedNote(id).observe(this, Observer {
                binding.reminderLocationTime.text =
                    it[0].locationName
                binding.title.text = it[0].title
                binding.description.text = it[0].description
            })
            binding.reminderIcon.setImageResource(R.drawable.ic_add_location)


        }


        binding.cancelReminder.setOnClickListener {
            cancelReminder()
        }

        binding.snoozeReminder.setOnClickListener {
            snoozeReminder()
        }

        binding.openApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        if (snoozeAlarm == true) {
            snoozeReminder()
        }

        if (dismissAlarm == true) {
            cancelReminder()
        }


    }

    private fun cancelReminder() {
        if (reminder == "location") {
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
                finish()
            })


        } else if (reminder == "time") {
            viewModel.selectedNote(id).observe(this, Observer { note ->
                val notes = note[0]
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
                finish()

            })


        } else if (snooze && reminder == "location") {
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
                finish()
            })
        }
    }

    private fun snoozeReminder() {
        viewModel.selectedNote(id).observe(this, Observer { notes ->
            notes?.let {
                val note = it[0]
                reminder?.let { remind ->
                    alarmReceiver.scheduleSnoozeAlarm(this, note, remind)
                    toast(this, "Alarm snoozed for five minutes")
                    if (remind == "time") {
                        val intentService = Intent(applicationContext, AlarmService::class.java)
                        applicationContext.stopService(intentService)
                        finish()
                    } else if (remind == "location") {
                        val intentService =
                            Intent(applicationContext, LocationReminderService::class.java)
                        applicationContext.stopService(intentService)
                        finish()
                    }
                }
            }
        })

    }

}