package tech.jhavidit.remindme.view.fragments

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import tech.jhavidit.remindme.databinding.FragmentSearchNotesBinding
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.view.adapters.SearchNoteAdapter
import tech.jhavidit.remindme.viewModel.NotesViewModel


class SearchNotesFragment : Fragment() {
    private lateinit var binding: FragmentSearchNotesBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: SearchNoteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSearchNotesBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        adapter = SearchNoteAdapter()
        binding.backBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.searchNote.requestFocus()
        val inputMethodManager =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.clearBtn.setOnClickListener {
            binding.searchNote.setText("")
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchNote.addTextChangedListener(textWatcher)

        return binding.root
    }

    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { str ->
                if (str.isEmpty()) {
                    binding.clearBtn.visibility = GONE
                    adapter.setNotes(listOf())
                } else {
                    binding.clearBtn.visibility = VISIBLE
                    viewModel.searchNote("%$str%").observe(viewLifecycleOwner, Observer {
                        log("searching $str $it")
                        adapter.setNotes(it)

                    })
                }

            }
        }
    }


}