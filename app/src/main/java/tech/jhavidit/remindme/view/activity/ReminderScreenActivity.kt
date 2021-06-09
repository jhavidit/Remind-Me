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
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.viewModel.NotesViewModel

class ReminderScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimeReminderBinding
    private lateinit var viewModel: NotesViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_time_reminder)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val bundle = intent?.getBundleExtra("notes")
        val reminder = bundle?.getString("reminder")
        binding.title.text = bundle?.getString("title")
        binding.description.text = bundle?.getString("description")
        binding.reminderLocationTime.text = bundle?.getString("locationName")
        log("notesMOdel = ${bundle?.getString("title")}")
        log("reminder = ${bundle?.toString()}")
        if (reminder == "location")
            binding.reminderIcon.setImageResource(R.drawable.ic_add_location)
        else if (reminder == "time")
            binding.reminderIcon.setImageResource(R.drawable.ic_alarm_set)
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