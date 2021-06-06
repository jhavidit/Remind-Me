package tech.jhavidit.remindme.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.internal.ContextUtils.getActivity
import kotlinx.android.synthetic.main.dialog_save_location.view.*
import tech.jhavidit.remindme.BuildConfig.MAPS_API_KEY
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityLocationSearchBinding
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.util.AUTOCOMPLETE_REQUEST_CODE
import tech.jhavidit.remindme.util.log
import tech.jhavidit.remindme.util.showLocationPermissionAlertDialog
import tech.jhavidit.remindme.view.adapters.SelectBackgroundColorAdapter
import tech.jhavidit.remindme.viewModel.LocationViewModel


class LocationSearchActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private var map: GoogleMap? = null
    private lateinit var viewModel: LocationViewModel
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var locationPlace: String = ""
    private var locationId: String = ""
    private var locationPermissionGranted = false
    private var notesModel: NotesModel? = null
    private var lat = 0.0
    private var lon = 0.0
    private lateinit var binding: ActivityLocationSearchBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_search)
        notesModel = intent?.getParcelableExtra<NotesModel>("notes")
        lat = intent?.getDoubleExtra("latitude", 0.0) ?: 0.0
        lon = intent?.getDoubleExtra("longitude", 0.0) ?: 0.0



        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, MAPS_API_KEY)
            placesClient = Places.createClient(this)
        }
        viewModel = ViewModelProvider(this).get(LocationViewModel::class.java)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val options = GoogleMapOptions()
        options.mapType(GoogleMap.MAP_TYPE_HYBRID)
            .compassEnabled(false)

        val locationButton =
            (findViewById<View>(Integer.parseInt("1")).parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.layoutParams as (RelativeLayout.LayoutParams)
        // position on right bottom
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 200);


        binding.selectLocation.setOnClickListener {
            showBottomSheet()
        }

        binding.searchBar.setOnClickListener {
            val fields = listOf(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME)
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(this)
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap?) {
        this.map = googleMap
        googleMap?.setOnMyLocationButtonClickListener(this)
        googleMap?.setOnMyLocationClickListener(this)
        enableLocation()
        googleMap?.apply {
            if (locationPermissionGranted)
                googleMap.isMyLocationEnabled = true
            else
                return
            val location = LatLng(lat, lon)
            moveCamera(CameraUpdateFactory.newLatLng(location))
            animateCamera(CameraUpdateFactory.zoomTo(15.0F))
        }
    }

    private fun enableLocation() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showLocationPermissionAlertDialog(this)
        } else
            locationPermissionGranted = true
    }

    override fun onMyLocationClick(p0: Location) {
        log("Location $p0")
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(data)
                        lat = place.latLng?.latitude ?: lat
                        lon = place.latLng?.longitude ?: lon
                        onMapReady(this.map)
                        locationPlace = place.name ?: ""
                        binding.searchLocationText.text = locationPlace
                        locationId = place.id ?: ""
                        log("Place: ${place.name}, ${place.id}")
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // TODO: Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        log(status.statusMessage)
                    }
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun showBottomSheet() {
        val bottomSheetDialogs = BottomSheetDialog(this)
        bottomSheetDialogs.setContentView(R.layout.bottom_sheet_save_location)
        val closeButton = bottomSheetDialogs.findViewById<ImageView>(R.id.close_btn_save_location)
        val locationName =
            bottomSheetDialogs.findViewById<TextView>(R.id.location_name_save_location)
        val saveButton = bottomSheetDialogs.findViewById<MaterialCardView>(R.id.save_location_card)
        locationName?.text = locationPlace

        closeButton?.setOnClickListener {
            bottomSheetDialogs.dismiss()
        }

        saveButton?.setOnClickListener {

            val latLng = map?.cameraPosition?.target
            latLng?.let {
                val locationModel = LocationModel(
                    id = 0,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    placeId = locationId,
                    name = locationPlace
                )
                viewModel.addLocation(locationModel)
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("notes", notesModel)
                intent.putExtra("location", locationModel)
                startActivity(intent)

            }

        }

        bottomSheetDialogs.show()
    }

}
