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
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetActiveReminderBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
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
        if (!args.currentNotes.locationReminder)
            binding.locationReminderCard.visibility = GONE
        else if (!args.currentNotes.timeReminder)
            binding.timeReminderCard.visibility = GONE
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
            geoFencingReceiver.cancelLocationReminder(requireContext(), args.currentNotes.id)
            val notes = NotesModel(
                id = args.currentNotes.id,
                title = args.currentNotes.title,
                description = args.currentNotes.description,
                locationReminder = false,
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
        }
        binding.deleteTimeReminder.setOnClickListener {
            alarmReceiver.cancelAlarm(requireContext(), args.currentNotes.id)
            val notes = NotesModel(
                id = args.currentNotes.id,
                title = args.currentNotes.title,
                description = args.currentNotes.description,
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = false,
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
        }
        return binding.root

    }

}