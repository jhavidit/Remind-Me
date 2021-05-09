package tech.jhavidit.remindme.view.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.databinding.FragmentLocationReminderBinding
import tech.jhavidit.remindme.view.activity.LocationSearchActivity
import tech.jhavidit.remindme.viewModel.NotesViewModel


class LocationReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLocationReminderBinding
    private lateinit var viewModel: NotesViewModel
    private val args: LocationReminderFragmentArgs by navArgs()
    private val LOCATION_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 200

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocationReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        binding.locationPicker.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), PERMISSION_REQUEST_CODE
                )
            } else {
                val intent = Intent(requireContext(), LocationSearchActivity::class.java)
                startActivity(intent)
            }
        }
        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode)
        {
            200 ->
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(requireContext(),LocationSearchActivity::class.java)
                    startActivity(intent)
                }
            else
                {
                    val alertDialog  : AlertDialog.Builder =  AlertDialog.Builder(requireContext())
                    alertDialog.setMessage("You need to provide location permission to access this feature. Kindly enable it from settings")
                    alertDialog.setCancelable(true)
                    alertDialog.setPositiveButton(
                        "Ok", DialogInterface.OnClickListener { dialog, which ->
                            val intent =
                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri =
                                Uri.fromParts("package", activity?.packageName, null)
                            intent.data = uri
                            startActivity(intent)
                        }
                    )
                    alertDialog.setNegativeButton(
                        "Cancel", DialogInterface.OnClickListener { dialog, which ->
                            dialog.cancel()
                        }
                    )
                    val alert = alertDialog.create()
                    alert.show()

                    }

        }
    }
}