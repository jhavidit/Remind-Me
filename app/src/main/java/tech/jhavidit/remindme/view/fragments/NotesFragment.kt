package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.view.adapters.NotesListAdapter
import tech.jhavidit.remindme.view.fragments.CreateNotesFragmentDirections.notesList
import tech.jhavidit.remindme.viewModel.NotesViewModel


class NotesFragment : Fragment() {
        private lateinit var  binding : FragmentNotesBinding
        private lateinit var navController: NavController
        private lateinit var adapter: NotesListAdapter
        private lateinit var viewModel : NotesViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentNotesBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        adapter = NotesListAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        viewModel.readAllData.observe(viewLifecycleOwner, Observer {
            adapter.setNotes(it)
        })
        val notes = NotesModel(0,"","")
        binding.fabAddNotes.setOnClickListener {
            navController.navigate(NotesFragmentDirections.updateNotes("created",notes))
        }
        return binding.root
    }

}