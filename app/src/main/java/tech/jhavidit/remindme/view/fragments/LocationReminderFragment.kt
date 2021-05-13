package tech.jhavidit.remindme.view.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import tech.jhavidit.remindme.databinding.FragmentLocationReminderBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.view.activity.LocationSearchActivity
import tech.jhavidit.remindme.viewModel.MainActivityViewModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class LocationReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLocationReminderBinding
    private lateinit var viewModel: NotesViewModel
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val args: LocationReminderFragmentArgs by navArgs()
    private lateinit var locationManager: LocationManager
    private val LOCATION_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 200
    private var hasLocation = false
    private var location: LocationModel? = null
    private lateinit var notesModel: NotesModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLocationReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)

        activityViewModel.locationModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                hasLocation = true
                location = it
                if (it.name.isNotEmpty()) {
                    binding.location.text = location?.name
                } else {
                    binding.location.text = "No Location Selected"
                }
            }
        })

        activityViewModel.notesModel.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                notesModel = it
            } else {
                notesModel = args.currentNotes
                binding.location.text = notesModel.locationName
                binding.radius.setText(notesModel.radius)
            }
            if (notesModel.locationReminder) {
                binding.cancelReminder.visibility = VISIBLE
                hasLocation = true
            } else {
                binding.cancelReminder.visibility = GONE
                binding.location.text = "No location selected"
            }
        })

        binding.cancelReminder.setOnClickListener {
            val notesModel = NotesModel(
                id = notesModel.id,
                title = notesModel.title,
                description = notesModel.description,
                locationReminder = false,
                timeReminder = notesModel.timeReminder,
                repeatAlarmIndex = notesModel.repeatAlarmIndex,
                reminderTime = notesModel.reminderTime
            )
            viewModel.updateNotes(notesModel)
            findNavController().navigate(LocationReminderFragmentDirections.homeScreen())
        }

        binding.saveLocationReminder.setOnClickListener {
            if (!hasLocation && binding.radius.text.isEmpty())
                Toast.makeText(
                    requireContext(),
                    "Location and Radius cannot be empty",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                val notesModel = NotesModel(
                    id = notesModel.id,
                    title = notesModel.title,
                    description = notesModel.description,
                    locationReminder = true,
                    timeReminder = notesModel.timeReminder,
                    latitude = location?.latitude.toString(),
                    longitude = location?.longitude?.toString(),
                    radius = binding.radius.text.toString(),
                    repeatAlarmIndex = notesModel.repeatAlarmIndex,
                    reminderTime = notesModel.reminderTime,
                    locationName = location?.name
                )
                viewModel.updateNotes(notesModel)
                findNavController().navigate(LocationReminderFragmentDirections.homeScreen())
            }
        }


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
                locationManager =
                    context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS()
                } else {
                    getLocation()
                }

            }
        }
        return binding.root
    }

    private fun onGPS() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Enable GPS").setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, which -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
            .setNegativeButton(
                "No"
            ) { dialog, which -> dialog.cancel() }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        val locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        locationGPS?.let {
            val lat = locationGPS.latitude
            val lon = locationGPS.longitude
            val intent = Intent(requireContext(), LocationSearchActivity::class.java)
            val notesModel = args.currentNotes
            intent.putExtra("latitude", lat)
            intent.putExtra("longitude", lon)
            intent.putExtra("notes", notesModel)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            200 ->
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(requireContext(), LocationSearchActivity::class.java)
                    startActivity(intent)
                } else {
                    val alertDialog: AlertDialog.Builder = AlertDialog.Builder(requireContext())
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

    override fun onDestroyView() {
        super.onDestroyView()
        activityViewModel.clearData()
    }

    override fun onDestroy() {
        super.onDestroy()
        activityViewModel.clearData()
    }
}