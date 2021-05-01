package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceControl
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentCreateNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class CreateNotesFragment : Fragment() {

    private lateinit var binding: FragmentCreateNotesBinding
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var notes: NotesModel
    private lateinit var navController: NavController
    private val args: CreateNotesFragmentArgs by navArgs()
    private var updated = false
    private var notesId = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateNotesBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        notes = args.currentNotes
        notesId = args.currentNotes.id
        if (args.update == "update") {
            updated = true
        }
        if (updated) {
            binding.notesEditHeading.text = "Update"
            binding.title.setText(notes.title)
            binding.description.setText(notes.description)
        }
        binding.btnTime.setOnClickListener {
            navController.navigate(CreateNotesFragmentDirections.timeReminder(args.currentNotes))
        }
        binding.btnLocation.setOnClickListener {
            notesViewModel.deleteNotes(notes)
            navController.navigateUp()
        }
        binding.title.addTextChangedListener(textWatcher)
        binding.description.addTextChangedListener(textWatcher)

        return binding.root
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

    private fun updateData() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val notesModel = NotesModel(notesId, title, description)
        notesViewModel.updateNotes(notesModel)

    }

    private fun insertDataToDatabase() {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val notesModel = NotesModel(0, title, description)
        notesViewModel.addNotes(notesModel)
        updated = true
        notesViewModel.createdId.observe(viewLifecycleOwner, Observer {
            notesId = it
        })
    }


}