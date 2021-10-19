package tech.jhavidit.remindme.view.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentNotesBinding
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.activity.ReminderScreenActivity
import tech.jhavidit.remindme.view.adapters.NotesListAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel


class NotesFragment : Fragment() {
    private lateinit var binding: FragmentNotesBinding
    private lateinit var navController: NavController
    private lateinit var adapter: NotesListAdapter
    private lateinit var viewModel: NotesViewModel
    private var notesCount = 0
    private var hasMissedReminder = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val bottomNavigation: CurvedBottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        bottomNavigation?.visibility = VISIBLE
        binding = FragmentNotesBinding.inflate(inflater, container, false)
        navController = Navigation.findNavController(requireActivity(), R.id.NavHostFragment)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NotesListAdapter()
        binding.searchBar.setOnClickListener {
            navController.navigate(NotesFragmentDirections.searchNotes())
        }
        binding.recyclerView.adapter = adapter
        val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
        binding.recyclerView.layoutManager = staggeredGridLayoutManager
        viewModel.readAllData.observe(viewLifecycleOwner, Observer {

            if (LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.ID) != null) {
                Snackbar.make(
                    binding.coordinatorLayout,
                    "You have an active reminder",
                    Snackbar.LENGTH_LONG
                ).setAction("View", View.OnClickListener {
                    val id = LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.ID)?.toInt()
                    val reminder =
                        LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.REMINDER)
                    var snooze = false
                    if (LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.SNOOZE) == "true")
                        snooze = true
                    val bundle = bundleOf("id" to id, "reminder" to reminder, "snooze" to snooze)
                    val intent = Intent(requireContext(), ReminderScreenActivity::class.java)
                    if (reminder == "time") {
                        intent.putExtra(NOTES_TIME, bundle)
                    } else
                        intent.putExtra(NOTES_LOCATION, bundle)
                    startActivity(intent)
                }).show()

            }

            it.forEach { note ->
                if (note.locationReminder == null && note.timeReminder == null && note.description.isEmpty() && note.title.isEmpty() && note.image==null) {
                    viewModel.deleteNotes(note)
                    Snackbar.make(
                        binding.coordinatorLayout,
                        "Empty note Discarded",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }

            }
            adapter.setNotes(it)
        })
        viewModel.notesCount.observe(viewLifecycleOwner, Observer {
            notesCount = it
        })

        binding.fabAddNotes.setOnClickListener {
            val notes = NotesModel(notesCount + 1, "", "")
            navController.navigate(NotesFragmentDirections.updateNotes(CREATE, notes))
        }
    }


}