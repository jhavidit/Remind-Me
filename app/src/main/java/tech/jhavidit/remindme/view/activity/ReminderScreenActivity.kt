package tech.jhavidit.remindme.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_create_notes.*
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
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
    private var id: Int = 0
    private var reminder: String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_reminder)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val notesTimeBundle = intent?.getBundleExtra(NOTES_TIME)
        notesTimeBundle?.let {
            id = notesTimeBundle.getInt("id")
            reminder = notesTimeBundle.getString("reminder")
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            binding.reminderLocationTime.text =
                notesTimeBundle.getString("reminderTime")
            binding.title.text = notesTimeBundle.getString("title")
            binding.description.text = notesTimeBundle.getString("description")

        }


        val notesLocationBundle = intent?.getBundleExtra(NOTES_LOCATION)

        notesLocationBundle?.let {
            id = notesLocationBundle.getInt("id")
//            viewModel.selectedNote(id)
//            viewModel.selectedNote.observe(this, Observer {
//                notesModelLocation = it.firstOrNull()
//            })
            reminder = notesLocationBundle.getString("reminder")
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            binding.reminderLocationTime.text =
                notesLocationBundle.getString("reminderTime")
            binding.title.text = notesLocationBundle.getString("title")
            binding.description.text = notesLocationBundle.getString("description")
            binding.reminderIcon.setImageResource(R.drawable.ic_add_location)
            binding.reminderLocationTime.text = notesLocationBundle.getString("locationName")


        }

        viewModel.selectedNote(id)

        binding.cancelReminder.setOnClickListener {
            cancelReminder()
        }
        binding.openApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun cancelReminder() {
        if (reminder == "location") {
            viewModel.selectedNote.observe(this, Observer { note ->
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
            viewModel.selectedNote.observe(this, Observer { note ->
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


        }
    }

}