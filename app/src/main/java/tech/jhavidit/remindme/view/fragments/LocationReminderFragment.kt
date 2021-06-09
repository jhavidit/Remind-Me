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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
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
    private lateinit var activityViewModel: MainActivityViewModel
    private val args: LocationReminderFragmentArgs by navArgs()
    private lateinit var locationManager: LocationManager
    private var selectedLocation: LocationModel? = null
    private var showLocation = false
    private lateinit var notesValue: NotesModel
    private lateinit var geoFencingHelper: GeoFencingHelper
    private lateinit var geoFencingReceiver: GeoFencingReceiver


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentLocationReminderBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        locationViewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        activityViewModel = ViewModelProvider(this).get(MainActivityViewModel::class.java)
        adapter = LocationNameAdapter(this)
        geoFencingReceiver = GeoFencingReceiver()
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
        binding.radiusValue.text = minRadius.toInt().toString()
        binding.minRadius.text = "${minRadius.toInt()}m"
        binding.maxRadius.text = "${maxRadius.toInt()}m"
        geoFencingClient = LocationServices.getGeofencingClient(requireContext())
        geoFencingHelper = GeoFencingHelper(requireContext())


        return binding.root
    }

    private fun onGPS() {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enable GPS")
            .setPositiveButton(
                "Enable"
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
            //  val notesModel = args.currentNotes
            intent.putExtra("latitude", lat)
            intent.putExtra("longitude", lon)
            intent.putExtra("notes", notesValue)
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
            showLocationPermissionAlertDialog(requireContext())
        }

    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
            return
        }

        var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

        val resultCode = when {
            runningQOrLater -> {

                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }

        requestPermissions(
            permissionsArray, resultCode
        )
    }

    override fun clickListener(location: LocationModel) {
        selectedLocation = location
        binding.selectedLocation.text = location.name
        binding.selectedLocation.alpha = 1F
        binding.recyclerView.visibility = GONE
        showLocation = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        locationViewModel.readAllData.observe(viewLifecycleOwner, Observer { location ->
            adapter.setLocation(location)
        })

        notesValue = args.currentNotes

        selectedLocation = arguments?.getParcelable("location")
        if (selectedLocation != null) {
            binding.selectedLocation.text = selectedLocation?.name
            binding.selectedLocation.alpha = 1F
        } else if (notesValue.locationName != null) {
            selectedLocation = LocationModel(
                id = 0,
                latitude = notesValue.latitude!!,
                longitude = notesValue.longitude!!,
                name = notesValue.locationName!!,
                placeId = ""
            )
            binding.selectedLocation.text = notesValue.locationName
            binding.selectedLocation.alpha = 1F
        } else {
            binding.selectedLocation.text = "Select or Add Location"
            binding.selectedLocation.alpha = 0.2F
        }

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

        binding.saveLocationCard.setOnClickListener {
            if (selectedLocation == null)
                Toast.makeText(
                    requireContext(),
                    "Invalid Location",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                val latitude = selectedLocation?.latitude
                val longitude = selectedLocation?.longitude
                if (latitude == null || longitude == null) {
                    toast(requireContext(), "Selected Location is invalid")
                } else if (foregroundAndBackgroundLocationPermissionApproved(requireContext())) {
                    geoFencingReceiver.addLocationReminder(
                        context = requireContext(),
                        id = notesValue.id,
                        latitude = latitude,
                        longitude = longitude,
                        radius = getRadius(minRadius, maxRadius, binding.radius.progress),
                        notesModel = notesValue
                    )
                    val notesModel = NotesModel(
                        id = notesValue.id,
                        title = notesValue.title,
                        description = notesValue.description,
                        locationReminder = true,
                        isPinned = notesValue.isPinned,
                        timeReminder = notesValue.timeReminder,
                        latitude = selectedLocation?.latitude,
                        longitude = selectedLocation?.longitude,
                        radius = getRadius(minRadius, maxRadius, binding.radius.progress),
                        repeatAlarmIndex = notesValue.repeatAlarmIndex,
                        reminderTime = notesValue.reminderTime,
                        locationName = selectedLocation?.name,
                        backgroundColor = notesValue.backgroundColor
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
    }
}
