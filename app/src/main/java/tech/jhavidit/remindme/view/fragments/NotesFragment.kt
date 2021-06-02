package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.CREATE
import tech.jhavidit.remindme.view.adapters.NotesListAdapter
import tech.jhavidit.remindme.view.adapters.SelectBackgroundColorAdapter
import tech.jhavidit.remindme.view.fragments.CreateNotesFragmentDirections.notesList
import tech.jhavidit.remindme.viewModel.MainActivityViewModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var navController: NavController
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private lateinit var adapter: NotesListAdapter
    private lateinit var viewModel: NotesViewModel
    private var notesCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val bottomNavigation: BottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        bottomNavigation?.visibility = VISIBLE
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        adapter = NotesListAdapter()
        binding.recyclerView.adapter = adapter
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        viewModel.readAllData.observe(viewLifecycleOwner, Observer {
            adapter.setNotes(it)
        })
        viewModel.notesCount.observe(viewLifecycleOwner, Observer {
            notesCount = it
        })

        binding.fabAddNotes.setOnClickListener {
            val notes = NotesModel(notesCount + 1, "", "")
            navController.navigate(NotesFragmentDirections.updateNotes(CREATE, notes))
        }
        return binding.root
    }

}