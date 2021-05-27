package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.databinding.BottomSheetReminderBinding

class ReminderSelectBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetReminderBinding
    private val args: ReminderSelectBottomSheetArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetReminderBinding.inflate(inflater, container, false)

        binding.timeReminderCard.setOnClickListener {
            findNavController().navigate(ReminderSelectBottomSheetDirections.timeReminder(args.currentNotes))
        }

        binding.locationReminderCard.setOnClickListener {
            findNavController().navigate(ReminderSelectBottomSheetDirections.locationReminder(args.currentNotes))
        }

        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        return binding.root
    }

}