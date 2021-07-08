package tech.jhavidit.remindme.view.fragments

import android.icu.text.AlphabeticIndex
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.LabelFormatter
import com.google.android.material.slider.RangeSlider
import com.google.android.material.slider.Slider
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.BottomSheetRadiusChooseBinding
import tech.jhavidit.remindme.util.log
import kotlin.math.roundToInt


class RadiusChooseBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: BottomSheetRadiusChooseBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = BottomSheetRadiusChooseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.radiusPicker.labelBehavior = LabelFormatter.LABEL_WITHIN_BOUNDS
        binding.radiusPicker.setLabelFormatter { value ->
            return@setLabelFormatter "${value.roundToInt()} m"
        }
        binding.radiusPicker.values = mutableListOf(100F,1000F)
        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }
        binding.radiusPicker.setMinSeparationValue(100F)
        binding.radiusPicker.addOnChangeListener { slider, _, _ ->
            log("Slider ${slider.values[0].roundToInt()}  ${slider.values[1].roundToInt()} ")
        }

    }


}