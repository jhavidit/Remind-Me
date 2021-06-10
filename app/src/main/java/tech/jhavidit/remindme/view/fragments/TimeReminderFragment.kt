package tech.jhavidit.remindme.view.fragments

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.databinding.FragmentTimeReminderBinding
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.view.adapters.TimePickerAdapter
import tech.jhavidit.remindme.viewModel.TimePickerViewModel

class TimeReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentTimeReminderBinding
    private lateinit var timeAdapter: TimePickerAdapter
    private lateinit var selectedViewModel: TimePickerViewModel
/*
    private lateinit var alarmReceiver: AlarmReceiver
    private val args: TimeReminderFragmentArgs by navArgs()
    private var repeatingIndex = 0
    private var reminderTIme: String = ""
    private lateinit var viewModel: NotesViewModel
*/

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTimeReminderBinding.inflate(inflater, container, false)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        selectedViewModel = ViewModelProvider(this).get(TimePickerViewModel::class.java)
        val time = arrayListOf<String>(
            "0",
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            ""
        )


        val padding = binding.timePicker.width / 2
        binding.recyclerView.setPadding(padding, 0, padding, 0)
        binding.recyclerView.apply {
            timeAdapter = TimePickerAdapter(requireContext(), binding.recyclerView)
            layoutManager = LinearLayoutManager(requireContext())
            adapter = timeAdapter
            setHasFixedSize(true)
            smoothScrollToPosition(3)
        }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
                val lastItem = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                log("$firstItem   $lastItem")
                if (time.size == 1) {
                    timeAdapter.setSelectedItem(0)
                } else if (lastItem == (time.size - 1)) {
                    timeAdapter.setSelectedItem(time.size - 2)
                } else {
                    timeAdapter.setSelectedItem(firstItem + 1)
                }
            }
        })


        /*  viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
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
          if (args.currentNotes.timeReminder == true)
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
              val notesModel = NotesModel(
                  id = args.currentNotes.id,
                  title = args.currentNotes.title,
                  description = args.currentNotes.description,
                  timeReminder = false,
                  reminderTime = null,
                  repeatAlarmIndex = repeatingIndex,
                  locationReminder = args.currentNotes.locationReminder,
                  latitude = args.currentNotes.latitude,
                  longitude = args.currentNotes.longitude,
                  radius = args.currentNotes.radius,
                  locationName = args.currentNotes.locationName,
                  backgroundColor = args.currentNotes.backgroundColor,
                  image = args.currentNotes.image,
                  lastUpdated = args.currentNotes.lastUpdated

              )
              viewModel.updateNotes(notesModel)
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
                  isPinned = args.currentNotes.isPinned,
                  repeatAlarmIndex = repeatingIndex,
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
  */




        return binding.root
    }
//
//    private fun initializeSelectedMonth() {
//        if (selectedViewModel.selectedValue.value == null) {
//            val now = selectedViewModel.selectedValue.value?:""
//            selectedViewModel.setSelectedValue(now)
//            scrollToMonth(now)
//        }
//    }

//    private fun scrollToMonth(month: String) {
//        var width = month_list.width
//
//        if (width > 0) {
//            val monthWidth = month_item.width
//            layoutManager.scrollToPositionWithOffset(mainViewModel.months.indexOf(month), width / 2 - monthWidth / 2)
//        }
//    }

}