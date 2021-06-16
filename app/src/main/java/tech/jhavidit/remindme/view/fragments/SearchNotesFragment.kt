package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentSearchNotesBinding
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.viewModel.NotesViewModel


class SearchNotesFragment : Fragment() {
    private lateinit var binding: FragmentSearchNotesBinding
    private lateinit var viewModel: NotesViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchNotesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        binding.searchNote.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchNote("%$query%").observe(viewLifecycleOwner, Observer {
                    log("searching $query $it")
                })
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.searchNote("%$newText%").observe(viewLifecycleOwner, Observer {
                    log("search $newText ${it.toString()}")
                })
                return true
            }

        })
        return binding.root
    }


}