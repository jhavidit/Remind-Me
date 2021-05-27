package tech.jhavidit.remindme.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetAddImageBinding

class AddImageBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding : BottomSheetAddImageBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  BottomSheetAddImageBinding.inflate(inflater, container, false)

        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        return binding.root
    }

}