package tech.jhavidit.remindme.view.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.service.AlarmService
import tech.jhavidit.remindme.util.NOTES_LOCATION
import tech.jhavidit.remindme.util.NOTES_TIME
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.viewModel.NotesViewModel

class ReminderScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeReminderBinding
    private lateinit var viewModel: NotesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_reminder)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val notesTimeBundle = intent?.getBundleExtra(NOTES_TIME)
        notesTimeBundle?.let {
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            binding.reminderLocationTime.text =
                notesTimeBundle.getString("reminderTime")
            binding.title.text = notesTimeBundle.getString("title")
            binding.description.text = notesTimeBundle.getString("description")

        }
        val notesLocationBundle = intent?.getBundleExtra(NOTES_LOCATION)

        notesLocationBundle?.let {
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
            binding.reminderLocationTime.text =
                notesLocationBundle.getString("reminderTime")
            binding.title.text = notesLocationBundle.getString("title")
            binding.description.text = notesLocationBundle.getString("description")
            binding.reminderIcon.setImageResource(R.drawable.ic_add_location)
            binding.reminderLocationTime.text = notesLocationBundle.getString("locationName")

        }

        binding.cancelReminder.setOnClickListener {
            val intentService = Intent(applicationContext, AlarmService::class.java)
            applicationContext.stopService(intentService)
            finish()
        }
        binding.openApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}