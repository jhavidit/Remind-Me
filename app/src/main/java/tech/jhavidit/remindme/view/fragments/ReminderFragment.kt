package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentRemindersBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.foregroundAndBackgroundLocationPermissionApproved
import tech.jhavidit.remindme.util.showLocationPermissionAlertDialog
import tech.jhavidit.remindme.util.toast
import tech.jhavidit.remindme.view.adapters.LocationReminderListAdapter
import tech.jhavidit.remindme.view.adapters.TimeReminderListAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel


class ReminderFragment : Fragment(), TimeReminderListAdapter.TimeReminderAdapterInterface,
    LocationReminderListAdapter.LocationReminderAdapterInterface {
    private lateinit var binding: FragmentRemindersBinding
    private lateinit var locationAdapter: LocationReminderListAdapter
    private lateinit var timeAdapter: TimeReminderListAdapter
    private lateinit var viewModel: NotesViewModel
    private lateinit var timeReminderList: List<NotesModel>
    private lateinit var locationReminderList: List<NotesModel>
    private lateinit var alarmReceiver: AlarmReceiver
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private var hasMissedReminder = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRemindersBinding.inflate(inflater, container, false)
        val bottomNavigation: CurvedBottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        bottomNavigation?.visibility = View.VISIBLE
        locationAdapter = LocationReminderListAdapter(this)
        timeAdapter = TimeReminderListAdapter(this)
        alarmReceiver = AlarmReceiver()
        geoFencingReceiver = GeoFencingReceiver()
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)

        viewModel.readAllData.observe(viewLifecycleOwner, Observer {
            it.forEach { note ->
                if (note.repeatValue != null && note.repeatValue == -1L && System.currentTimeMillis() > note.reminderWaitTime!!) {
                    hasMissedReminder = true
                }
            }
            if (hasMissedReminder)
                Snackbar.make(
                    binding.coordinatorLayout,
                    "You have missed/snoozed time reminders. Kindly check",
                    Snackbar.LENGTH_SHORT
                ).show()
        })



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.readAllData.observe(viewLifecycleOwner, Observer { notes ->
            timeReminderList = notes?.filter {
                it.reminderTime != null
            } ?: listOf()

            locationReminderList = notes?.filter {
                it.locationName != null
            } ?: listOf()

            if (binding.rbReminder.checkedRadioButtonId == R.id.rb_time) {
                if (timeReminderList.isEmpty()) {
                    binding.recyclerView.visibility = GONE
                    binding.lottieAnimation.visibility = VISIBLE
                    binding.lottieText.visibility = VISIBLE
                    binding.lottieAnimation.setAnimation(R.raw.time_reminder)
                    binding.lottieAnimation.playAnimation()
                    binding.lottieText.text = String.format("%s\n%s", "No Active Time", "Reminders")
                } else {
                    binding.recyclerView.visibility = VISIBLE
                    binding.lottieText.visibility = GONE
                    binding.lottieAnimation.visibility = GONE
                    binding.recyclerView.adapter = timeAdapter
                    timeAdapter.setNotes(timeReminderList)
                }
            } else {
                if (locationReminderList.isEmpty()) {
                    binding.recyclerView.visibility = GONE
                    binding.lottieAnimation.visibility = VISIBLE
                    binding.lottieText.visibility = VISIBLE
                    binding.lottieAnimation.setAnimation(R.raw.location_reminder)
                    binding.lottieAnimation.playAnimation()
                    binding.lottieText.text = String.format("%s\n%s", "No Active Location", "Reminders")
                } else {
                    binding.recyclerView.visibility = VISIBLE
                    binding.lottieText.visibility = GONE
                    binding.lottieAnimation.visibility = GONE
                    binding.recyclerView.adapter = locationAdapter
                    locationAdapter.setNotes(locationReminderList)
                }
            }

            binding.rbReminder.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId == R.id.rb_location) {
                    if (locationReminderList.isEmpty()) {
                        binding.recyclerView.visibility = GONE
                        binding.lottieAnimation.visibility = VISIBLE
                        binding.lottieText.visibility = VISIBLE
                        binding.lottieAnimation.setAnimation(R.raw.location_reminder)
                        binding.lottieAnimation.playAnimation()
                        binding.lottieText.text =
                            String.format("%s\n%s", "No Active Location", "Reminders")
                    } else {
                        binding.recyclerView.visibility = VISIBLE
                        binding.lottieText.visibility = GONE
                        binding.lottieAnimation.visibility = GONE
                        binding.recyclerView.adapter = locationAdapter
                        locationAdapter.setNotes(locationReminderList)
                    }
                } else if (checkedId == R.id.rb_time) {
                    if (timeReminderList.isEmpty()) {
                        binding.recyclerView.visibility = GONE
                        binding.lottieAnimation.visibility = VISIBLE
                        binding.lottieText.visibility = VISIBLE
                        binding.lottieAnimation.setAnimation(R.raw.time_reminder)
                        binding.lottieAnimation.playAnimation()
                        binding.lottieText.text = String.format("%s\n%s", "No Active Time", "Reminders")
                    } else {
                        binding.recyclerView.visibility = VISIBLE
                        binding.lottieText.visibility = GONE
                        binding.lottieAnimation.visibility = GONE
                        binding.recyclerView.adapter = timeAdapter
                        timeAdapter.setNotes(timeReminderList)
                    }
                }
            }
        })
    }

    override fun disableEnableTimeReminder(checked: Boolean, notesModel: NotesModel) {
        if (checked) {
            alarmReceiver.scheduleAlarm(requireContext(), notesModel)
            val notes = NotesModel(
                id = notesModel.id,
                title = notesModel.title,
                description = notesModel.description,
                locationReminder = notesModel.locationReminder,
                timeReminder = true,
                reminderWaitTime = notesModel.reminderWaitTime,
                reminderTime = notesModel.reminderTime,
                reminderDate = notesModel.reminderDate,
                latitude = notesModel.latitude,
                image = notesModel.image,
                longitude = notesModel.longitude,
                isPinned = notesModel.isPinned,
                radius = notesModel.radius,
                repeatValue = notesModel.repeatValue,
                locationName = notesModel.locationName,
                backgroundColor = notesModel.backgroundColor,
                lastUpdated = notesModel.lastUpdated
            )
            viewModel.updateNotes(notes)
        } else {
            alarmReceiver.cancelAlarm(requireContext(), notesModel.id)
            val notes = NotesModel(
                id = notesModel.id,
                title = notesModel.title,
                description = notesModel.description,
                locationReminder = notesModel.locationReminder,
                timeReminder = false,
                reminderWaitTime = notesModel.reminderWaitTime,
                reminderTime = notesModel.reminderTime,
                reminderDate = notesModel.reminderDate,
                latitude = notesModel.latitude,
                image = notesModel.image,
                longitude = notesModel.longitude,
                isPinned = notesModel.isPinned,
                radius = notesModel.radius,
                repeatValue = notesModel.repeatValue,
                locationName = notesModel.locationName,
                backgroundColor = notesModel.backgroundColor,
                lastUpdated = notesModel.lastUpdated
            )
            viewModel.updateNotes(notes)
        }
    }

    override fun disableEnableLocationReminder(checked: Boolean, notesModel: NotesModel) {
        if (checked) {
            if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
                geoFencingReceiver.addLocationReminder(
                    context = requireContext(),
                    id = notesModel.id,
                    latitude = notesModel.latitude!!,
                    longitude = notesModel.longitude!!,
                    radius = notesModel.radius!!,
                    notesModel = notesModel
                )
                val notes = NotesModel(
                    id = notesModel.id,
                    title = notesModel.title,
                    description = notesModel.description,
                    locationReminder = true,
                    timeReminder = notesModel.timeReminder,
                    image = notesModel.image,
                    reminderTime = notesModel.reminderTime,
                    reminderWaitTime = notesModel.reminderWaitTime,
                    reminderDate = notesModel.reminderDate,
                    latitude = notesModel.latitude,
                    longitude = notesModel.longitude,
                    isPinned = notesModel.isPinned,
                    radius = notesModel.radius,
                    repeatValue = notesModel.repeatValue,
                    locationName = notesModel.locationName,
                    backgroundColor = notesModel.backgroundColor,
                    lastUpdated = notesModel.lastUpdated
                )
                viewModel.updateNotes(notes)
            } else {
                showLocationPermissionAlertDialog(requireContext())
            }
        } else {
            geoFencingReceiver.cancelLocationReminder(requireContext(), notesModel.id)
            val notes = NotesModel(
                id = notesModel.id,
                title = notesModel.title,
                description = notesModel.description,
                locationReminder = false,
                timeReminder = notesModel.timeReminder,
                image = notesModel.image,
                reminderTime = notesModel.reminderTime,
                reminderWaitTime = notesModel.reminderWaitTime,
                reminderDate = notesModel.reminderDate,
                latitude = notesModel.latitude,
                longitude = notesModel.longitude,
                isPinned = notesModel.isPinned,
                radius = notesModel.radius,
                repeatValue = notesModel.repeatValue,
                locationName = notesModel.locationName,
                backgroundColor = notesModel.backgroundColor,
                lastUpdated = notesModel.lastUpdated
            )
            viewModel.updateNotes(notes)
        }
    }


}