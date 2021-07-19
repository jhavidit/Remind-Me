package tech.jhavidit.remindme.view.fragments

import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import np.com.susanthapa.curved_bottom_navigation.CurvedBottomNavigationView
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.FragmentSettingsBinding
import tech.jhavidit.remindme.receiver.AlarmReceiver
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.LocalKeyStorage
import tech.jhavidit.remindme.util.RINGTONE_RESULT_CODE
import tech.jhavidit.remindme.util.getNameFromUri
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.viewModel.NotesViewModel


class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private lateinit var notesViewModel: NotesViewModel
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private lateinit var alarmReceiver: AlarmReceiver
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val bottomNavigation: CurvedBottomNavigationView? = activity?.findViewById(R.id.bottom_nav)
        notesViewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        alarmReceiver = AlarmReceiver()
        geoFencingReceiver = GeoFencingReceiver()
        bottomNavigation?.visibility = View.VISIBLE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.RINGTONE_NAME)
            ?.let { ringtoneName ->
                binding.ringtoneName.text = ringtoneName
            }
        binding.doNotDisturbSwitch.isChecked =
            LocalKeyStorage(requireContext()).getValue(LocalKeyStorage.DO_NOT_DISTURB) == "true"
        binding.doNotDisturb.setOnClickListener {
            binding.doNotDisturbSwitch.isChecked = !binding.doNotDisturbSwitch.isChecked
        }
        binding.doNotDisturbSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                LocalKeyStorage(requireContext()).saveValue(LocalKeyStorage.DO_NOT_DISTURB, "true")
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Do not Disturb Activated",
                    Snackbar.LENGTH_SHORT
                ).show()


            } else {
                LocalKeyStorage(requireContext()).saveValue(LocalKeyStorage.DO_NOT_DISTURB, "false")
                Snackbar.make(
                    binding.coordinatorLayout,
                    "Do not Disturb Deactivated",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }

        binding.radiusLimit.setOnClickListener {
            findNavController().navigate(SettingsFragmentDirections.chooseRadiusRange())
        }

        binding.ringtone.setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, RingtoneManager.TYPE_ALARM)
            startActivityForResult(intent, RINGTONE_RESULT_CODE)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RINGTONE_RESULT_CODE) {
            log(data.toString())
            val uri: Uri? = data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)

            uri?.let {
                LocalKeyStorage(requireContext()).saveValue(
                    LocalKeyStorage.RINGTONE,
                    it.toString()
                )
                val name = getNameFromUri(requireContext(), it)
                name?.let { ringtoneName ->
                    LocalKeyStorage(requireContext()).saveValue(
                        LocalKeyStorage.RINGTONE_NAME,
                        ringtoneName
                    )
                    binding.ringtoneName.text = ringtoneName
                } ?: run {
                    LocalKeyStorage(requireContext()).saveValue(
                        LocalKeyStorage.RINGTONE_NAME,
                        "Unknown"
                    )
                }
            }
        }
    }

}