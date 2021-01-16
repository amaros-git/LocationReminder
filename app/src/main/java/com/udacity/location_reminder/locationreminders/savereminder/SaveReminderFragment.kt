package com.udacity.location_reminder.locationreminders.savereminder

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.udacity.location_reminder.BuildConfig
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

    //When navigate back showing Snackbar with an action, on the different screen app crashes
    //with "not attached to Activity". Thus I will remove Snackbar once this Fragment is destroyed
    private var snackBarGoToSettings: Snackbar? = null


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

                _viewModel.saveReminder(reminder)

                _viewModel.saveGeofenceAndNavigateBackIfSuccess(
                    reminder.id,
                    reminder.latitude!!,
                    reminder.longitude!!
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
        checkPermissionsAndRequestIfMissing()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()

        snackBarGoToSettings?.dismiss()
    }

    private fun checkPermissionsAndRequestIfMissing() {
        if (!isForegroundLocationPermissionAllowed()) {
            requestForegroundLocationPermission()
        } else if (!isBackgroundLocationPermissionAllowed()) {
            requestBackgroundLocationPermission()
        }
    }

    private fun isForegroundLocationPermissionAllowed(): Boolean =
        PackageManager.PERMISSION_GRANTED == checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        )

    @TargetApi(29)
    private fun isBackgroundLocationPermissionAllowed(): Boolean {
        return if (runningQOrLater) {
            PackageManager.PERMISSION_GRANTED == checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        } else {
            true //if Android OS < Q version, simply return true.
        }
    }

    private fun requestForegroundLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE
        )
    }

    @TargetApi(29)
    private fun requestBackgroundLocationPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE
        )
    }

    //On Android 10+ (Q) toi use geofences we need to have the background permission as well.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
       /* Log.d(TAG, "requestCode = $requestCode")

        Log.d(TAG, "received permissions:")
        permissions.forEach {
            Log.d(TAG, it)
        }

        Log.d(TAG, "received grantResults:")
        grantResults.forEach {
            Log.d(TAG, it.toString())
        }*/

        if (grantResults.isEmpty()) {
            return
        }

        when (requestCode) {
            REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE -> {
                if ((grantResults[0] == PackageManager.PERMISSION_GRANTED) &&
                    !isBackgroundLocationPermissionAllowed()
                ) {
                    requestBackgroundLocationPermission()
                } else {
                    snackBarGoToSettings = showToastWithSettingsAction(
                        binding.root,
                        R.string.permission_denied_explanation
                    ).apply {
                        show()
                    }
                }
            }

            REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE -> {
                if ((grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    snackBarGoToSettings = showToastWithSettingsAction(
                        binding.root,
                        R.string.permission_denied_explanation
                    ).apply {
                        show()
                    }
                }
            }
        }
    }

    private fun showToastWithSettingsAction(
        view: View,
        textRId: Int,
        length: Int = Snackbar.LENGTH_INDEFINITE
    ): Snackbar {
        Log.d(TAG, "isAdded = $isAdded")
        return Snackbar.make(view, textRId, length).apply {
            setAction(R.string.settings) {
                // Displays App settings screen.
                startActivity(Intent().apply {
                    action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }

    companion object {
        internal const val ACTION_GEOFENCE_EVENT =
            "LocationReminder.action.ACTION_GEOFENCE_EVENT"
    }
}

private const val LOCATION_PERM_INDEX = 0
private const val BACKGROUND_LOCATION_PERM_INDEX = 1

private const val REQUEST_FOREGROUND_PERMISSION_REQUEST_CODE = 34
private const val REQUEST_BACKGROUND_PERMISSION_REQUEST_CODE = 33



