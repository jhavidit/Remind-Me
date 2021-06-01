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
import tech.jhavidit.remindme.viewModel.NotesViewModel

class ActiveReminderBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetActiveReminderBinding
    private lateinit var viewModel: NotesViewModel
    private val args: ActiveReminderBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetActiveReminderBinding.inflate(inflater, container, false)
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
        return binding.root

    }

}