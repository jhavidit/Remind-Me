package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetActiveReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.viewModel.NotesViewModel

class ActiveReminderBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetActiveReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private lateinit var alarmReceiver: AlarmReceiver
    private val args: ActiveReminderBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetActiveReminderBinding.inflate(inflater, container, false)
        geoFencingReceiver = GeoFencingReceiver()
        alarmReceiver = AlarmReceiver()
        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        if (args.reminderType == TIME) {
            binding.locationReminderCard.visibility = GONE
            binding.timeReminderSwitch.isChecked = args.currentNotes.timeReminder ?: false
            binding.reminderTime.text = args.currentNotes.reminderTime.toString()
            binding.reminderRepeat.text = args.currentNotes.repeatAlarmIndex.toString()
        } else if (args.reminderType == LOCATION) {
            binding.timeReminderCard.visibility = GONE
            binding.locationReminderSwitch.isChecked = args.currentNotes.locationReminder ?: false
            binding.locationName.text = args.currentNotes.locationName
            binding.locationRadius.text = "Radius - ${args.currentNotes.radius?.toInt()}m"
        }
        binding.editLocationReminder.setOnClickListener {
            findNavController().navigate(
                ActiveReminderBottomSheetDirections.editLocationReminder(
                    args.currentNotes
                )
            )
        }
        binding.editTimeReminder.setOnClickListener {
            findNavController().navigate(ActiveReminderBottomSheetDirections.editTimeReminder(args.currentNotes))
        }
        binding.deleteLocationReminder.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are you sure want to delete this location reminder?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->
                    geoFencingReceiver.cancelLocationReminder(
                        requireContext(),
                        args.currentNotes.id
                    )
                    val notes = NotesModel(
                        id = args.currentNotes.id,
                        title = args.currentNotes.title,
                        description = args.currentNotes.description,
                        locationReminder = null,
                        timeReminder = args.currentNotes.timeReminder,
                        reminderTime = args.currentNotes.reminderTime,
                        isPinned = args.currentNotes.isPinned,
                        latitude = null,
                        longitude = null,
                        radius = null,
                        repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                        locationName = null,
                        backgroundColor = args.currentNotes.backgroundColor
                    )
                    viewModel.updateNotes(notes)
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.notesFragment)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()


        }
        binding.timeReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                alarmReceiver.scheduleAlarm(requireContext(), args.currentNotes)
                val notes = NotesModel(
                    id = args.currentNotes.id,
                    title = args.currentNotes.title,
                    description = args.currentNotes.description,
                    locationReminder = args.currentNotes.locationReminder,
                    timeReminder = true,
                    reminderTime = args.currentNotes.reminderTime,
                    latitude = args.currentNotes.latitude,
                    longitude = args.currentNotes.longitude,
                    isPinned = args.currentNotes.isPinned,
                    radius = args.currentNotes.radius,
                    repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                    locationName = args.currentNotes.locationName,
                    backgroundColor = args.currentNotes.backgroundColor,
                    lastUpdated = args.currentNotes.lastUpdated
                )
                viewModel.updateNotes(notes)
            } else {
                alarmReceiver.cancelAlarm(requireContext(), args.currentNotes.id)
                val notes = NotesModel(
                    id = args.currentNotes.id,
                    title = args.currentNotes.title,
                    description = args.currentNotes.description,
                    locationReminder = args.currentNotes.locationReminder,
                    timeReminder = false,
                    reminderTime = args.currentNotes.reminderTime,
                    latitude = args.currentNotes.latitude,
                    longitude = args.currentNotes.longitude,
                    isPinned = args.currentNotes.isPinned,
                    radius = args.currentNotes.radius,
                    repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                    locationName = args.currentNotes.locationName,
                    backgroundColor = args.currentNotes.backgroundColor,
                    lastUpdated = args.currentNotes.lastUpdated
                )
                viewModel.updateNotes(notes)
            }
        }

        binding.locationReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
                    geoFencingReceiver.addLocationReminder(
                        context = requireContext(),
                        id = args.currentNotes.id,
                        latitude = args.currentNotes.latitude!!,
                        longitude = args.currentNotes.longitude!!,
                        radius = args.currentNotes.radius!!,
                        notesModel = args.currentNotes
                    )
                    val notes = NotesModel(
                        id = args.currentNotes.id,
                        title = args.currentNotes.title,
                        description = args.currentNotes.description,
                        locationReminder = args.currentNotes.locationReminder,
                        timeReminder = true,
                        reminderTime = args.currentNotes.reminderTime,
                        latitude = args.currentNotes.latitude,
                        longitude = args.currentNotes.longitude,
                        isPinned = args.currentNotes.isPinned,
                        radius = args.currentNotes.radius,
                        repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                        locationName = args.currentNotes.locationName,
                        backgroundColor = args.currentNotes.backgroundColor,
                        lastUpdated = args.currentNotes.lastUpdated
                    )
                    viewModel.updateNotes(notes)
                } else {
                    showLocationPermissionAlertDialog(requireContext())
                }
            } else {
                geoFencingReceiver.cancelLocationReminder(requireContext(), args.currentNotes.id)
                val notes = NotesModel(
                    id = args.currentNotes.id,
                    title = args.currentNotes.title,
                    description = args.currentNotes.description,
                    locationReminder = args.currentNotes.locationReminder,
                    timeReminder = false,
                    reminderTime = args.currentNotes.reminderTime,
                    latitude = args.currentNotes.latitude,
                    longitude = args.currentNotes.longitude,
                    isPinned = args.currentNotes.isPinned,
                    radius = args.currentNotes.radius,
                    repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                    locationName = args.currentNotes.locationName,
                    backgroundColor = args.currentNotes.backgroundColor,
                    lastUpdated = args.currentNotes.lastUpdated
                )
                viewModel.updateNotes(notes)

            }

        }

        binding.deleteTimeReminder.setOnClickListener {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are you sure want to delete this time reminder?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->
                    alarmReceiver.cancelAlarm(requireContext(), args.currentNotes.id)
                    val notes = NotesModel(
                        id = args.currentNotes.id,
                        title = args.currentNotes.title,
                        description = args.currentNotes.description,
                        locationReminder = args.currentNotes.locationReminder,
                        timeReminder = null,
                        reminderTime = null,
                        latitude = args.currentNotes.latitude,
                        longitude = args.currentNotes.longitude,
                        isPinned = args.currentNotes.isPinned,
                        radius = args.currentNotes.radius,
                        repeatAlarmIndex = -1,
                        locationName = args.currentNotes.locationName,
                        backgroundColor = args.currentNotes.backgroundColor,
                        lastUpdated = args.currentNotes.lastUpdated
                    )
                    viewModel.updateNotes(notes)
                    findNavController().popBackStack()
                    findNavController().navigate(R.id.notesFragment)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

        }
        return binding.root

    }

}