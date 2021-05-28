package tech.jhavidit.remindme.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
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
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.jhavidit.remindme.databinding.FragmentLocationReminderBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.activity.LocationSearchActivity
import tech.jhavidit.remindme.viewModel.MainActivityViewModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class LocationReminderFragment : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentLocationReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var geoFencingClient: GeofencingClient
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val args: LocationReminderFragmentArgs by navArgs()
    private lateinit var locationManager: LocationManager
    private var hasLocation = false
    private var location: LocationModel? = null
    private lateinit var notesModel: NotesModel
    private lateinit var geoFencingHelper: GeoFencingHelper
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLocationReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)

        activityViewModel.locationModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                hasLocation = true
                location = it
                binding.location.text = location?.name
            }
        })

        activityViewModel.notesModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                notesModel = it
            } ?: run {
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

        geoFencingClient = LocationServices.getGeofencingClient(requireContext())
        geoFencingHelper = GeoFencingHelper(requireContext())
        binding.cancelReminder.setOnClickListener {
            val notesModel = NotesModel(
                id = notesModel.id,
                title = notesModel.title,
                description = notesModel.description,
                locationReminder = false,
                isPinned = notesModel.isPinned,
                timeReminder = notesModel.timeReminder,
                repeatAlarmIndex = notesModel.repeatAlarmIndex,
                reminderTime = notesModel.reminderTime,
                backgroundColor = notesModel.backgroundColor
            )
            viewModel.updateNotes(notesModel)
            findNavController().navigate(LocationReminderFragmentDirections.homeScreen())
        }

        binding.saveLocationReminder.setOnClickListener {
            if (!hasLocation && binding.radius.text.length < 3)
                Toast.makeText(
                    requireContext(),
                    "Invalid Location or Radius",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                addGeofence(
                    latitude = location?.latitude,
                    longitude = location?.longitude,
                    radius = binding.radius.text.toString().toDouble()
                )
                val notesModel = NotesModel(
                    id = notesModel.id,
                    title = notesModel.title,
                    description = notesModel.description,
                    locationReminder = true,
                    isPinned = notesModel.isPinned,
                    timeReminder = notesModel.timeReminder,
                    latitude = location?.latitude.toString(),
                    longitude = location?.longitude?.toString(),
                    radius = binding.radius.text.toString(),
                    repeatAlarmIndex = notesModel.repeatAlarmIndex,
                    reminderTime = notesModel.reminderTime,
                    locationName = location?.name,
                    backgroundColor = notesModel.backgroundColor
                )
                viewModel.updateNotes(notesModel)
                findNavController().navigate(LocationReminderFragmentDirections.homeScreen())
            }
        }


        binding.locationPicker.setOnClickListener {
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                locationManager =
                    context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    onGPS()
                } else {
                    getLastKnownLocation()
                }

            } else
                requestForegroundAndBackgroundLocationPermissions()
        }
        return binding.root
    }


    @SuppressLint("MissingPermission")
    private fun addGeofence(latitude: Double?, longitude: Double?, radius: Double) {
        if (latitude != null && longitude != null) {
            val geofence: Geofence? = geoFencingHelper.getGeofence(
                id = notesModel.id.toString(),
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                transitionTypes = Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL or Geofence.GEOFENCE_TRANSITION_EXIT
            )
            val geofencingRequest = geoFencingHelper.getGeoFencingRequest(geofence)
            val pendingIntent = geoFencingHelper.getPendingIntent()
            if (foregroundAndBackgroundLocationPermissionApproved()) {
                geoFencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener {
                        log("Granted Geofencing successfully")
                    }
                    .addOnFailureListener {
                        log("error in geofence " + it.cause)
                    }
            }
        } else {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }


    private fun onGPS() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enable GPS")
            .setPositiveButton(
                "Delete"
            ) { _, _ ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton(
                "Cancel"
            ) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .show()
    }


    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            val mLocationManager =
                requireContext().getSystemService(LOCATION_SERVICE) as LocationManager
            val providers: List<String> = mLocationManager.getProviders(true)
            var bestLocation: Location? = null
            for (provider in providers) {
                val l: Location = mLocationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    // Found best last known location: %s", l);
                    bestLocation = l
                }
            }
            val lat = bestLocation?.latitude
            val lon = bestLocation?.longitude
            val intent = Intent(requireContext(), LocationSearchActivity::class.java)
            val notesModel = args.currentNotes
            intent.putExtra("latitude", lat)
            intent.putExtra("longitude", lon)
            intent.putExtra("notes", notesModel)
            startActivity(intent)
        } else
            requestForegroundAndBackgroundLocationPermissions()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (
            grantResults.isEmpty() ||
            grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
            (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                    grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                    PackageManager.PERMISSION_DENIED)
        ) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Location Permission Required")
                .setMessage("You need to provide location permission to access this feature. Kindly enable it from settings")
                .setPositiveButton(
                    "Ok"
                ) { dialogInterface, i ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, i ->
                    dialogInterface.dismiss()
                }
                .show()
        }

    }

    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ))
        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED ==
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        )
            } else {
                true
            }

        return foregroundLocationApproved && backgroundPermissionApproved
    }


    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved()) {
            return
        }

        // Else request the permission
        // this provides the result[LOCATION_PERMISSION_INDEX]
        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        requestPermissions(
            permissionsArray, resultCode
        )
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
