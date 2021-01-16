package com.udacity.location_reminder.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.BaseFragment
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.databinding.FragmentSaveReminderBinding
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.location_reminder.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment() {

    private val TAG = SaveReminderFragment::class.java.simpleName

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()

    private lateinit var binding: FragmentSaveReminderBinding

    private val runningQOrLater =
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value

            val reminder = ReminderDataItem(title, description, location, latitude, longitude)
            if (_viewModel.validateEnteredData(reminder)) {
                _viewModel.saveGeofence(
                    reminder.id,
                    reminder.latitude!!,
                    reminder.longitude!!
                )
                _viewModel.saveReminderAndNavigateBack(reminder)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        checkPermissionsAndRequestIfMissing()
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    /**
     * Starts the permission check and Geofence process only if the Geofence associated with the
     * current hint isn't yet active.
     */
    private fun checkPermissionsAndRequestIfMissing() {
        if (!isForegroundAndBackgroundLocationPermissionApproved()) {
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @TargetApi(29)
    private fun isForegroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved =
            PackageManager.PERMISSION_GRANTED == checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            )

        val backgroundPermissionApproved =
            if (runningQOrLater) {
                PackageManager.PERMISSION_GRANTED == checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            } else {
                true
            }

        Log.d(TAG, "foregroundLocationApproved = $foregroundLocationApproved, backgroundPermissionApproved = $backgroundPermissionApproved")
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    @TargetApi(29)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (isForegroundAndBackgroundLocationPermissionApproved()) {
            return
        }

        //var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
       var permissionsArray = arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        //var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        val resultCode = REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
       /* val resultCode = when {
            runningQOrLater -> {
                Log.d(TAG, "Requesting background")
                // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
            }
            else -> REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE
        }*/

        requestPermissions(permissionsArray, resultCode)
    }


    //On Android 10+ (Q) toi use geofences we need to have the background permission as well.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.d(TAG, "requestCode = $requestCode")

        Log.d(TAG, "received permissions:")
        permissions.forEach {
            Log.d(TAG, it)
        }

        Log.d(TAG, "received grantResults:")
        grantResults.forEach {
            Log.d(TAG, it.toString())
        }

        if (grantResults.isNotEmpty()) {
            if ((grantResults[LOCATION_PERM_INDEX] == PackageManager.PERMISSION_GRANTED)) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                ) {
                    requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE
                    )
                }
            }
        }
        Log.d(TAG, "Permissions NOT granted")


      /*  if (grantResults.isNotEmpty()) {
            if ((grantResults[LOCATION_PERM_INDEX] == PackageManager.PERMISSION_GRANTED) &&
                (grantResults[BACKGROUND_LOCATION_PERM_INDEX] == PackageManager.PERMISSION_GRANTED)
            ) {
                Log.d(TAG, "Permissions granted")
                return
            }
        }
        Log.d(TAG, "Permissions NOT granted")*/

         /* if (
              grantResults.isEmpty() ||
              grantResults[LOCATION_PERMISSION_INDEX] == PackageManager.PERMISSION_DENIED ||
              (requestCode == REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE &&
                      grantResults[BACKGROUND_LOCATION_PERMISSION_INDEX] ==
                      PackageManager.PERMISSION_DENIED)
          ) {
              // Permission denied.
              Snackbar.make(
                  binding.root,
                  R.string.permission_denied_explanation, Snackbar.LENGTH_SHORT
              )
                  .setAction(R.string.settings) {
                      // Displays App settings screen.
                      startActivity(Intent().apply {
                          action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                          data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                          flags = Intent.FLAG_ACTIVITY_NEW_TASK
                      })
                  }.show()
          } else {
              //checkDeviceLocationSettingsAndStartGeofence()
          }*/
    }
    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "LocationReminder.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val LOCATION_PERM_INDEX = 0
private const val BACKGROUND_LOCATION_PERM_INDEX = 1

private const val REQUEST_FOREGROUND_AND_BACKGROUND_PERMISSION_RESULT_CODE = 33
private const val REQUEST_FOREGROUND_ONLY_PERMISSIONS_REQUEST_CODE = 34


