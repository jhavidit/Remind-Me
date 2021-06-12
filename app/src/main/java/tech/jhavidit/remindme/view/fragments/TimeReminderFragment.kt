package tech.jhavidit.remindme.view.fragments

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentTimeReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.model.RepeatHourModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.viewModel.NotesViewModel
import java.util.*
import kotlin.collections.ArrayList

class TimeReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentTimeReminderBinding
    private lateinit var alarmReceiver: AlarmReceiver
    private val args: TimeReminderFragmentArgs by navArgs()
    private var reminderTime = ""
    private var reminderDate = ""
    private var repeatingValue: Long = -1
    private var reminderTIme: String = ""
    private lateinit var repeatList: Array<String>
    private lateinit var viewModel: NotesViewModel

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimeReminderBinding.inflate(inflater, container, false)

        repeatList = RepeatHourModel.getRepeatingHours()

        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.repeaterSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.repeatLayout.visibility = VISIBLE
            } else {
                binding.repeatLayout.visibility = GONE
            }
        }



        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        alarmReceiver = AlarmReceiver()
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        var alarmHour = c.get(Calendar.HOUR_OF_DAY)
        var alarmMinute = c.get(Calendar.MINUTE)
        var alarmYear = 0
        var alarmMonth = 0
        var alarmDay = 0


        val datePickerDialog = DatePickerDialog(
            requireContext(),
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                // Display Selected date in textbox
                alarmYear = year
                alarmMonth = monthOfYear
                alarmDay = dayOfMonth
                binding.calendarText.text = "$dayOfMonth/$monthOfYear/$year"
                reminderDate = "$dayOfMonth/$monthOfYear/$year"
            },
            year,
            month,
            day
        )

        binding.timePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            alarmHour = hourOfDay
            alarmMinute = minute
            if (hourOfDay == 12)
                reminderTime = "$hourOfDay:$minute PM"
            else if (hourOfDay < 12)
                reminderTIme = "$hourOfDay:$minute AM"
            else
                reminderTIme = "${hourOfDay - 12}:$minute PM"
        }

        binding.repeatPicker.apply {
            minValue = 1
            maxValue = repeatList.size
            displayedValues = repeatList
            value = 7
        }


        binding.datePicker.setOnClickListener {
            datePickerDialog.show()
        }

        binding.hours.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.repeatPicker.visibility = VISIBLE
                binding.weekly.isChecked = false
                binding.daily.isChecked = false
                binding.monthly.isChecked = false
            }
        }

        binding.daily.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.repeatPicker.visibility = GONE
                binding.weekly.isChecked = false
                binding.hours.isChecked = false
                binding.monthly.isChecked = false
            }
        }

        binding.weekly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.repeatPicker.visibility = GONE
                binding.hours.isChecked = false
                binding.daily.isChecked = false
                binding.monthly.isChecked = false
            }
        }

        binding.monthly.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.repeatPicker.visibility = GONE
                binding.weekly.isChecked = false
                binding.daily.isChecked = false
                binding.hours.isChecked = false
            }
        }


        binding.repeaterSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                TransitionManager.beginDelayedTransition(binding.repeatLayout)
                binding.repeatLayout.visibility = VISIBLE
                binding.repeatPicker.visibility = VISIBLE
                binding.weekly.isChecked = false
                binding.daily.isChecked = false
                binding.monthly.isChecked = false
                binding.hours.isChecked = true

            } else {
                binding.repeatLayout.visibility = GONE
            }
        }

        binding.saveReminderCard.setOnClickListener {
            c.set(alarmYear, alarmMonth, alarmDay, alarmHour, alarmMinute, 0)
            val alarmTime = c.timeInMillis
            val currentTime = Calendar.getInstance().timeInMillis

            if (!binding.repeaterSwitch.isChecked)
                repeatingValue = -1
            else if (binding.hours.isChecked)
                repeatingValue =
                    repeatList[binding.repeatPicker.value].toLong() * AlarmManager.INTERVAL_HOUR
            else if (binding.weekly.isChecked) {
                repeatingValue = AlarmManager.INTERVAL_DAY * 7
            } else if (binding.daily.isChecked) {
                repeatingValue = AlarmManager.INTERVAL_DAY
            } else if (binding.monthly.isChecked) {
                repeatingValue = AlarmManager.INTERVAL_DAY * 30
            }
            if (alarmDay == 0 && alarmMonth == 0 && alarmYear == 0) {
                toast(requireContext(), "Please Select Date")
            } else if (currentTime > alarmTime) {
                toast(requireContext(), "Reminder Time cannot be less than current time")
            } else {
                val notes = NotesModel(
                    id = args.currentNotes.id,
                    title = args.currentNotes.title,
                    description = args.currentNotes.description,
                    timeReminder = true,
                    reminderTime = reminderTime,
                    reminderWaitTime = alarmTime,
                    reminderDate = reminderDate,
                    isPinned = args.currentNotes.isPinned,
                    repeatValue = repeatingValue,
                    locationReminder = args.currentNotes.locationReminder,
                    latitude = args.currentNotes.latitude,
                    longitude = args.currentNotes.longitude,
                    radius = args.currentNotes.radius,
                    locationName = args.currentNotes.locationName,
                    backgroundColor = args.currentNotes.backgroundColor,
                    image = args.currentNotes.image,
                    lastUpdated = args.currentNotes.lastUpdated
                )
                viewModel.updateNotes(notes)
                alarmReceiver.scheduleAlarm(requireContext(), notes)
                findNavController().navigate(R.id.notesFragment)
            }
        }

        return binding.root
    }

}