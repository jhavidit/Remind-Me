package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentSearchNotesBinding


class SearchNotesFragment : Fragment() {
    private lateinit var binding: FragmentSearchNotesBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchNotesBinding.inflate(inflater, container, false)
        return binding.root
    }




}