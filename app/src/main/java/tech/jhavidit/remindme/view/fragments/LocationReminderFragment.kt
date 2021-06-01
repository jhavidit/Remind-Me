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
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import tech.jhavidit.remindme.databinding.FragmentLocationReminderBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.receiver.GeoFencingReceiver
import tech.jhavidit.remindme.util.*
import tech.jhavidit.remindme.view.activity.LocationSearchActivity
import tech.jhavidit.remindme.view.adapters.LocationNameAdapter
import tech.jhavidit.remindme.viewModel.LocationViewModel
import tech.jhavidit.remindme.viewModel.MainActivityViewModel
import tech.jhavidit.remindme.viewModel.NotesViewModel


class LocationReminderFragment : BottomSheetDialogFragment(),
    LocationNameAdapter.LocationNameInterface {
    private lateinit var binding: FragmentLocationReminderBinding
    private lateinit var viewModel: NotesViewModel
    private lateinit var adapter: LocationNameAdapter
    private lateinit var geoFencingClient: GeofencingClient
    private var minRadius: Double = 100.0
    private var maxRadius: Double = 1000.0
    private lateinit var locationViewModel: LocationViewModel
    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val args: LocationReminderFragmentArgs by navArgs()
    private lateinit var locationManager: LocationManager
    private var hasLocation = false
    private var selectedLocation: LocationModel? = null
    private var showLocation = false
    private lateinit var notesModel: NotesModel
    private lateinit var geoFencingHelper: GeoFencingHelper
    private lateinit var geoFencingReceiver: GeoFencingReceiver
    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentLocationReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        adapter = LocationNameAdapter(this)
        geoFencingReceiver = GeoFencingReceiver()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.radiusValue.text = minRadius.toInt().toString()
        binding.minRadius.text = "${minRadius.toInt()} m"
        binding.maxRadius.text = "${maxRadius.toInt()} m"
        locationViewModel.readAllData.observe(viewLifecycleOwner, Observer {
            adapter.setLocation(it)
        })

        binding.closeBtn.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.downBtn.setOnClickListener {
            if (!showLocation) {
                TransitionManager.beginDelayedTransition(binding.selectedLocationCard)
                showLocation = true
                binding.recyclerView.visibility = VISIBLE
            } else {
                showLocation = false
                binding.recyclerView.visibility = GONE
            }
        }
        activityViewModel.locationModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                hasLocation = true
                selectedLocation = it
                binding.selectedLocation.text = selectedLocation?.name
            }
        })

        activityViewModel.notesModel.observe(viewLifecycleOwner, Observer {
            it?.let {
                notesModel = it
            } ?: run {
                notesModel = args.currentNotes
                binding.selectedLocation.text = notesModel.locationName
                // binding.radius.progress = notesModel.radius?.toInt() ?: 0
            }
        })



        geoFencingClient = LocationServices.getGeofencingClient(requireContext())
        geoFencingHelper = GeoFencingHelper(requireContext())
        /* binding.cancelReminder.setOnClickListener {
             val notesModel = NotesModel(
                 id = notesModel.id,
                 title = notesModel.title,
                 description = notesModel.description,
                 locationReminder = false,
                 isPinned = notesModel.isPinned,
                 timeReminder = notesModel.timeReminder,
                 repeatAlarmIndex = notesModel.repeatAlarmIndex,
                 reminderTime = notesModel.reminderTime,
                 backgroundColor = notesModel.backgroundColor,
                 image = notesModel.image,
                 lastUpdated = notesModel.lastUpdated
             )
             viewModel.updateNotes(notesModel)
             findNavController().navigate(LocationReminderFragmentDirections.homeScreen())
         }*/

        binding.saveLocationCard.setOnClickListener {
            if (!hasLocation)
                Toast.makeText(
                    requireContext(),
                    "Invalid Location",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                val latitude = selectedLocation?.latitude
                val longitude = selectedLocation?.longitude
                if (latitude == null || longitude == null) {
                    toast(requireContext(), "Select Location is invalid")
                } else if (!foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
                    geoFencingReceiver.addLocationReminder(
                        context = requireContext(),
                        id = notesModel.id,
                        latitude = latitude,
                        longitude = longitude,
                        radius = getRadius(minRadius, maxRadius, binding.radius.progress)
                    )
                    val notesModel = NotesModel(
                        id = notesModel.id,
                        title = notesModel.title,
                        description = notesModel.description,
                        locationReminder = true,
                        isPinned = notesModel.isPinned,
                        timeReminder = notesModel.timeReminder,
                        latitude = selectedLocation?.latitude.toString(),
                        longitude = selectedLocation?.longitude?.toString(),
                        radius = getRadius(minRadius, maxRadius, binding.radius.progress),
                        repeatAlarmIndex = notesModel.repeatAlarmIndex,
                        reminderTime = notesModel.reminderTime,
                        locationName = selectedLocation?.name,
                        backgroundColor = notesModel.backgroundColor
                    )

                    viewModel.updateNotes(notesModel)
                    findNavController().navigate(LocationReminderFragmentDirections.homeScreen())

                } else {
                    requestForegroundAndBackgroundLocationPermissions()
                }
            }
        }

        binding.radius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

                seekBar?.let {
                    binding.radiusValue.text =
                        getRadius(minRadius, maxRadius, progress).toInt().toString()
                    val width = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
                    val thumbPos = seekBar.paddingLeft + width * seekBar.progress / seekBar.max

                    binding.radiusMarker.measure(0, 0)
                    val txtW: Int = binding.radiusMarker.measuredWidth
                    val delta = txtW / 2
                    binding.radiusMarker.x = seekBar.x + thumbPos - delta
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

        binding.locationPicker.setOnClickListener {
            if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
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
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
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
                ) { _, _ ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", activity?.packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                .setNegativeButton(
                    "Cancel"
                ) { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .show()
        }

    }

    /* @TargetApi(29)
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
     }*/


    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
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

    override fun clickListener(location: LocationModel) {
        selectedLocation = location
        binding.selectedLocation.text = location.name
    }
}
