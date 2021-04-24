package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentCreateNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class CreateNotesFragment : Fragment() {

    private lateinit var binding  : FragmentCreateNotesBinding
    private lateinit var notesViewModel : NotesViewModel
    private lateinit var notes : NotesModel
    private val args : CreateNotesFragmentArgs by navArgs()
    private var updated = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCreateNotesBinding.inflate(inflater, container, false)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        notes = args.currentNotes
        if(args.update =="update")
            updated = true
        binding.btnSave.setOnClickListener {
            if(updated) {
                val title = binding.title.text.toString()
                val description = binding.description.text.toString()
                val notesModel = NotesModel(args.currentNotes.id,title,description)
                notesViewModel.updateNotes(notesModel)
                findNavController().navigate(R.id.notesFragment)
            }
            else
            insertDataToDatabase()
        }

        if(updated) {
            binding.notesEditHeading.text = "Update"
            binding.btnSave.text = "Update"
            binding.title.setText(notes.title)
            binding.description.setText(notes.description)

        }


        return binding.root
    }

    private fun insertDataToDatabase()
    {
        val title = binding.title.text.toString()
        val description = binding.description.text.toString()
        val notesModel = NotesModel(0,title,description)
        notesViewModel.addNotes(notesModel)

        findNavController().navigate(R.id.notesFragment)
    }


}