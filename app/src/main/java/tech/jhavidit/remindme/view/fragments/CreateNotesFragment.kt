package tech.jhavidit.remindme.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetAddColorBinding
import tech.jhavidit.remindme.databinding.FragmentCreateNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.getPeriod
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.toDateFormat
import tech.jhavidit.remindme.view.adapters.SelectBackgroundColorAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log


class CreateNotesFragment : Fragment(), SelectBackgroundColorAdapter.AdapterInterface {

    private lateinit var binding: FragmentCreateNotesBinding
    private lateinit var bind: BottomSheetAddColorBinding
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notes: NotesModel
    private lateinit var navController: NavController
    private var isPinned = false
    private val args: CreateNotesFragmentArgs by navArgs()
    private var updated = false
    private var notesId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateNotesBinding.inflate(inflater, container, false)
        bind = BottomSheetAddColorBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        binding.note.setBackgroundColor(Color.parseColor(args.currentNotes.backgroundColor))
        notes = args.currentNotes
        notesId = args.currentNotes.id
        val lastUpdated = args.currentNotes.lastUpdated?.toDateFormat()?.let { getPeriod(it) }

        lastUpdated?.let { binding.lastUpdated.text = "Last edit : ${lastUpdated}" }
            ?: kotlin.run {
                binding.lastUpdated.text = "Last edit : recently"
            }


        if (notes.isPinned) {
            isPinned = true
            binding.pinBtn.setImageResource(R.drawable.ic_pin)
        } else {
            isPinned = false
            binding.pinBtn.setImageResource(R.drawable.ic_unpin)
        }

        val bottomNavigation: BottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        bottomNavigation?.visibility = GONE
        if (args.update == "update") {
            updated = true
        }
        if (updated) {
            binding.title.setText(notes.title)
            binding.description.setText(notes.description)
        }

        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }



        binding.pinBtn.setOnClickListener {
            if (isPinned) {
                isPinned = false
                binding.pinBtn.setImageResource(R.drawable.ic_unpin)
            } else {
                isPinned = true
                binding.pinBtn.setImageResource(R.drawable.ic_pin)
            }
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                latitude = args.currentNotes.latitude,
                isPinned = isPinned,
                longitude = args.currentNotes.longitude,
                radius = args.currentNotes.radius,
                repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                locationName = args.currentNotes.locationName,
                backgroundColor = args.currentNotes.backgroundColor
            )
            notesViewModel.updateNotes(notes)
        }

        binding.deleteBtn.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Are you sure want to delete this note?")
                .setPositiveButton(
                    "Delete"
                ) { _, _ ->
                    notesViewModel.deleteNotes(notes)
                    navController.navigateUp()
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()

        }

        binding.shareBtn.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "${binding.title.text}\n${binding.description.text}")
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        binding.colorBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            showColorBottomSheet()
        }

        binding.uploadImageBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                isPinned = args.currentNotes.isPinned,
                latitude = args.currentNotes.latitude,
                longitude = args.currentNotes.longitude,
                radius = args.currentNotes.radius,
                repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                locationName = args.currentNotes.locationName,
                backgroundColor = args.currentNotes.backgroundColor
            )
            findNavController().navigate(CreateNotesFragmentDirections.uploadImage(notes))
        }

        binding.reminderBtn.setOnClickListener {
            if (!updated)
                insertDataToDatabase()
            val notes = NotesModel(
                id = notesId,
                title = binding.title.text.toString(),
                description = binding.description.text.toString(),
                locationReminder = args.currentNotes.locationReminder,
                timeReminder = args.currentNotes.timeReminder,
                reminderTime = args.currentNotes.reminderTime,
                isPinned = args.currentNotes.isPinned,
                latitude = args.currentNotes.latitude,
                longitude = args.currentNotes.longitude,
                radius = args.currentNotes.radius,
                repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
                locationName = args.currentNotes.locationName,
                backgroundColor = args.currentNotes.backgroundColor
            )
            navController.navigate(CreateNotesFragmentDirections.addReminder(notes))
        }

        binding.title.addTextChangedListener(textWatcher)
        binding.description.addTextChangedListener(textWatcher)

        return binding.root
    }

    private fun showColorBottomSheet() {
        val bottomSheetDialogs = BottomSheetDialog(requireContext())
        bottomSheetDialogs.setContentView(R.layout.bottom_sheet_add_color)
        val recyclerView =
            bottomSheetDialogs.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.color_recycler_view)
        val closeButton = bottomSheetDialogs.findViewById<ImageView>(R.id.close_btn_color)

        val adapter = SelectBackgroundColorAdapter(this)
        recyclerView?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView?.adapter = adapter

        closeButton?.setOnClickListener {
            bottomSheetDialogs.dismiss()
        }

        bottomSheetDialogs.show()


    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (updated)
                updateData()
            else {
                insertDataToDatabase()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun updateData() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()

        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val formattedDate: String = dateFormat.format(calendar.time)

        val notesModel = NotesModel(
            id = notesId,
            title = title,
            description = description,
            locationReminder = args.currentNotes.locationReminder,
            timeReminder = args.currentNotes.timeReminder,
            reminderTime = args.currentNotes.reminderTime,
            latitude = args.currentNotes.latitude,
            longitude = args.currentNotes.longitude,
            isPinned = args.currentNotes.isPinned,
            radius = args.currentNotes.radius,
            repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
            locationName = args.currentNotes.locationName,
            backgroundColor = args.currentNotes.backgroundColor,
            lastUpdated = formattedDate
        )
        notesViewModel.updateNotes(notesModel)
    }

    private fun insertDataToDatabase() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val notesModel = NotesModel(0, title, description)
        notesViewModel.addNotes(notesModel)
        updated = true
        notesViewModel.createdId.observe(viewLifecycleOwner, Observer {
            notesId = it ?: 0
        })
    }

    override fun clickListener(color: String) {
        changeBackground(color)
    }

    private fun changeBackground(color: String) {
        binding.note.setBackgroundColor(Color.parseColor(color))
        val notes = NotesModel(
            id = notesId,
            title = binding.title.text.toString(),
            description = binding.description.text.toString(),
            locationReminder = args.currentNotes.locationReminder,
            timeReminder = args.currentNotes.timeReminder,
            reminderTime = args.currentNotes.reminderTime,
            latitude = args.currentNotes.latitude,
            longitude = args.currentNotes.longitude,
            isPinned = args.currentNotes.isPinned,
            radius = args.currentNotes.radius,
            repeatAlarmIndex = args.currentNotes.repeatAlarmIndex,
            locationName = args.currentNotes.locationName,
            backgroundColor = color,
            lastUpdated = args.currentNotes.lastUpdated
        )
        notesViewModel.updateNotes(notes)
    }


}