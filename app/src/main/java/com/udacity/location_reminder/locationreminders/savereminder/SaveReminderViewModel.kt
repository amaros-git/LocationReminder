package com.udacity.location_reminder.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.provider.Settings.Global.getString
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.BaseViewModel
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SaveReminderViewModel(val app: Application, private val dataSource: ReminderDataSource) :
    BaseViewModel(app) {

    private val TAG = SaveReminderViewModel::class.java.simpleName

    val reminderTitle = MutableLiveData<String?>()
    val reminderDescription = MutableLiveData<String?>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        latitude.value = null
        longitude.value = null
    }

    /**
     * Save the reminder to the data source
     */
    fun saveReminder(reminderData: ReminderDataItem) {
        Log.d(TAG, "saveReminder called")
        showLoading.value = true
        viewModelScope.launch {
            dataSource.saveReminder(
                ReminderDTO(
                    reminderData.title!!,
                    reminderData.description,
                    reminderData.location,
                    reminderData.latitude!!,
                    reminderData.longitude!!,
                    reminderData.id
                )
            )
            Log.d(TAG, "Saved reminder with id ${reminderData.id} ")
            showLoading.value = false
            showToast.value = app.getString(R.string.reminder_saved)
            navigationCommand.value = NavigationCommand.Back
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showErrorMessage.value = app.getString(R.string.err_enter_title)
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showErrorMessage.value = app.getString(R.string.err_select_location)
            return false
        }

        if ((null == reminderData.latitude) || (null == reminderData.longitude)) {
            showErrorMessage.value = app.getString(R.string.err_select_location)
            return false
        }

        return true
    }
}
