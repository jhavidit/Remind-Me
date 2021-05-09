package tech.jhavidit.remindme.view.activity

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import tech.jhavidit.remindme.R
import tech.jhavidit.remindme.databinding.ActivityLocationSearchBinding


class LocationSearchActivity : AppCompatActivity(), OnMapReadyCallback {
    private var map: GoogleMap? = null
    private var cameraPosition: CameraPosition? = null
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)

    private val lat = 26.4841
    private val lon = 80.2759
    private lateinit var binding: ActivityLocationSearchBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_search)
        val apiKey = getString(R.string.api_key)

//        if (savedInstanceState != null) {
//            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
//            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
//        }

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, apiKey);
        }
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
        val autocompleteFragment =
            supportFragmentManager.findFragmentById(R.id.autocomplete_fragment) as AutocompleteSupportFragment?
        autocompleteFragment?.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.LAT_LNG,
                Place.Field.NAME
            )
        )
        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(p0: Place) {
                Log.d("location", p0.toString())
            }

            override fun onError(p0: Status) {
                Log.d("error", p0.toString())
            }

        })

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.apply {
            val location = LatLng(lat, lon)
            addMarker(
                MarkerOptions()
                    .position(location)
                    .title("Marker in Sydney")
            )
            moveCamera(CameraUpdateFactory.newLatLng(location))
        }
    }
}
