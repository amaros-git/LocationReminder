package com.udacity.location_reminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.BaseFragment
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.databinding.FragmentSelectLocationBinding
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.location_reminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    private val TAG = SelectLocationFragment::class.java.simpleName

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var map: GoogleMap

    private lateinit var locationManager: LocationManager

    private var selectedLocationLatLng: LatLng? = null
    private var selectedLocationName = "Location"

    private var currentMarker: Marker? = null

    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSelectLocationBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        registerLocationListener()


        binding.saveLocationButton.setOnClickListener {
            if (null == selectedLocationLatLng) {
                _viewModel.showToast.value = "Please select location"
            } else {
                onLocationSelected(selectedLocationLatLng!!)
            }
        }

        return binding.root
    }

    private fun onLocationSelected(location: LatLng) {
        if (validateSelectedLocation()) {
            _viewModel.latitude.value = location.latitude
            _viewModel.longitude.value = location.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedLocationName

            _viewModel.navigationCommand.value = NavigationCommand.Back
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        if (isPermissionGranted()) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 10f, this)
        } else {
            requestForPermission()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        enableMyLocation()

        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }

        setMapLongClick(map)

        setPoiClick(map)

        setMapStyle(map)
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        if (isPermissionGranted()) {
            map.isMyLocationEnabled = true
        } else {
            requestForPermission()
        }
    }

    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            currentMarker?.remove()

            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            ).apply {
                showInfoWindow()
            }
            
            selectedLocationLatLng = poi.latLng
            selectedLocationName = poi.name
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            currentMarker?.remove()

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            currentMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            selectedLocationLatLng = latLng
            selectedLocationName = "Custom location"
        }
    }

    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Log.d(TAG, "Google Map style parsing error")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find google map style. Error: $e")
        }
    }

    private fun requestForPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION_PERMISSION
        )
    }

    private fun isPermissionGranted(): Boolean {
        return checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
                registerLocationListener()
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged called")
        val latLng = LatLng(location.latitude, location.longitude)
        val zoomLevel = 12.0f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        //unregister, we need the current location only once
        locationManager.removeUpdates(this)
    }

   /* override fun onProviderEnabled(provider: String) {
        Log.d(TAG, "onProviderEnabled called")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d(TAG, "onProviderDisabled called")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        Log.d(TAG, "onStatusChanged called")
    }*/

    private fun validateSelectedLocation(): Boolean {
        if (null == selectedLocationLatLng) {
            _viewModel.showToast.value = "Please select location"
            return false
        }
        return true
    }
}
