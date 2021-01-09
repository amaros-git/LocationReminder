package com.udacity.location_reminder.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.BaseFragment
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.databinding.FragmentSelectLocationBinding
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.location_reminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import org.koin.androidx.scope.currentScope
import java.util.*


class SelectLocationFragment : BaseFragment(), OnMapReadyCallback, LocationListener {

    private val TAG = SelectLocationFragment::class.java.simpleName

    private val REQUEST_LOCATION_PERMISSION = 1

    private lateinit var map: GoogleMap

    private lateinit var locationManager: LocationManager

    private var selectedLocation: LatLng? = null

    //Use Koin to get the view model of the SaveReminder
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

        locationManager = activity!!.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        registerLocationListener()


        binding.saveLocationButton.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if (null == selectedLocation) {
            _viewModel.showToast.value = "Please select location"
            return
        } else {
            _viewModel.latitude.value = selectedLocation!!.latitude
            _viewModel.longitude.value = selectedLocation!!.longitude

            _viewModel.navigationCommand.value = NavigationCommand.Back
        }


        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
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

    //TODO: add style to the map
    override fun onMapReady(googleMap: GoogleMap) {
        Log.d(TAG, "onMapReady called")
        map = googleMap

        enableMyLocation()

        with(map.uiSettings) {
            isZoomControlsEnabled = true
            isCompassEnabled = true
        }

        setMapLongClick(map)

        setPoiClick(map)
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
            Log.d(TAG, "clicked on Poi with ${poi.latLng}")
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker.showInfoWindow()

            selectedLocation = poi.latLng
        }
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            Log.d(TAG, "clicked on $latLng")

            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )

            selectedLocation = latLng
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
            context!!,
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
        val zoomLevel = 20.0f
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))

        //unregister, we need the current location only once
        locationManager.removeUpdates(this)
    }
}
