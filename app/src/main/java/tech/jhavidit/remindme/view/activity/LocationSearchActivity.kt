package tech.jhavidit.remindme.view.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import tech.jhavidit.remindme.BuildConfig.MAPS_API_KEY
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityLocationSearchBinding


class LocationSearchActivity : AppCompatActivity(), OnMapReadyCallback,
    GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener {
    private var map: GoogleMap? = null
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private val TAG = "tag"
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var locationPermissionGranted = false


    private var lat = 26.4841
    private var lon = 80.2759
    private lateinit var binding: ActivityLocationSearchBinding
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_search)

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, MAPS_API_KEY)
            placesClient = Places.createClient(this)
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)

        val options = GoogleMapOptions()
        options.mapType(GoogleMap.MAP_TYPE_HYBRID)
            .compassEnabled(false)

        binding.selectLocation.setOnClickListener {
            val latLng = map?.cameraPosition?.target
            Log.d("lat", latLng.toString())

        }

        binding.searchBar.setOnSearchClickListener {


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
            animateCamera(CameraUpdateFactory.zoomTo(11.0F))

            setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(p0: Marker?) {
                    lat = p0?.position?.latitude ?: lat
                    lon = p0?.position?.longitude ?: lon
                    Log.d("lat", "$lat   $lon")
                }

                override fun onMarkerDragStart(p0: Marker?) {
                }

                override fun onMarkerDrag(p0: Marker?) {
                }
            })
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
            val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)
            alertDialog.setMessage("You need to provide location permission to access this feature. Kindly enable it from settings")
            alertDialog.setCancelable(true)
            alertDialog.setPositiveButton(
                "Ok", DialogInterface.OnClickListener { dialog, which ->
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri: Uri =
                        Uri.fromParts("package", packageName, null)
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

        } else
            locationPermissionGranted = true


    }

    override fun onMyLocationClick(p0: Location) {
        Log.d("location", p0.toString())
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Location Button Clicked", Toast.LENGTH_SHORT).show()
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        data?.let {
                            val place = Autocomplete.getPlaceFromIntent(data)
                            lat = place.latLng?.latitude ?: lat
                            lon = place.latLng?.longitude ?: lon
                            onMapReady(this.map)
                            Log.i(TAG, "Place: ${place.name}, ${place.id}")
                        }
                    }
                    AutocompleteActivity.RESULT_ERROR -> {
                        // TODO: Handle the error.
                        data?.let {
                            val status = Autocomplete.getStatusFromIntent(data)
                            Log.i(TAG, status.statusMessage)
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
        super.onActivityResult(requestCode, resultCode, data)

    }
}
