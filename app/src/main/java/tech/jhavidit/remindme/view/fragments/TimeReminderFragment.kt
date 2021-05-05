package tech.jhavidit.remindme.view.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.viewModel.NotesViewModel
import java.util.*

class TimeReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentTimeReminderBinding
    private lateinit var alarmReceiver: AlarmReceiver
    private val args: TimeReminderFragmentArgs by navArgs()
    private var repeatingIndex = 0
    private var reminderTIme: String = ""
    private lateinit var viewModel: NotesViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimeReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        val mTimePicker: TimePickerDialog
        alarmReceiver = AlarmReceiver()
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var alarmHour = c.get(Calendar.HOUR_OF_DAY)
        var alarmMinute = c.get(Calendar.MINUTE)
        var alarmYear = c.get(Calendar.YEAR)
        var alarmMonth = c.get(Calendar.MONTH)
        var alarmDay = c.get(Calendar.DAY_OF_MONTH)
        if (args.currentNotes.timeReminder)
            binding.cancelAlarm.visibility = VISIBLE
        else
            binding.cancelAlarm.visibility = GONE

        val repeating = resources.getStringArray(R.array.Repeating)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                alarmYear = year
                alarmMonth = monthOfYear
                alarmDay = dayOfMonth
                binding.datePickerText.text = "" + dayOfMonth + "/" + monthOfYear + "/" + year
                reminderTIme = "" + dayOfMonth + "/" + monthOfYear + "/" + year

            },
            year,
            month,
            day
        )



        mTimePicker = TimePickerDialog(
            requireContext(),
            TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                alarmHour = hourOfDay
                alarmMinute = minute
                binding.timePickerText.text = String.format("%d : %d", hourOfDay, minute)
                reminderTIme = reminderTIme + " " + String.format("%d : %d", hourOfDay, minute)
            }, hour, minute, false

        )

        mTimePicker.setTitle("Set Reminder Time")

        binding.timePicker.setOnClickListener {
            mTimePicker.show()
        }
        binding.datePicker.setOnClickListener {
            datePickerDialog.show()
        }

        val spinner = binding.spinner
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item, repeating
        )
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                repeatingIndex = position
            }
        }

        binding.cancelAlarm.setOnClickListener {
            alarmReceiver.cancelAlarm(requireContext(), args.currentNotes.id)
            findNavController().navigateUp()
        }


        binding.save.setOnClickListener {
            c.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute, 0)
            val alarmTime = c.timeInMillis

            val notes = NotesModel(
                id = args.currentNotes.id,
                title = args.currentNotes.title,
                description = args.currentNotes.description,
                timeReminder = true,
                reminderTime = alarmTime,
                repeatAlarmIndex = repeatingIndex
            )
            viewModel.updateNotes(notes)
            alarmReceiver.scheduleAlarm(requireContext(), notes)
            findNavController().navigateUp()

        }

        return binding.root
    }
}