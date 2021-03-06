package com.udacity.location_reminder.locationreminders.reminderslist

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.udacity.location_reminder.base.BaseViewModel
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.data.dto.Result
import com.udacity.location_reminder.locationreminders.geofence.GeofenceClient
import kotlinx.coroutines.launch


class RemindersListViewModel(
    private val app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {

    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()

    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun loadReminders() {
        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            val result = dataSource.getReminders()
            showLoading.postValue(false)
            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message!!
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }

    fun deleteAllReminders() {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteAllReminders()
            val geofenceClient = GeofenceClient(app)
            geofenceClient.removeAllGeofences()
            loadReminders()

            showLoading.value = false
        }
    }

    fun deleteReminder(reminderId: String) {
        showLoading.value = true
        viewModelScope.launch {
            dataSource.deleteReminder(reminderId)

            val geofenceClient = GeofenceClient(app)
            geofenceClient.removeGeofences(listOf(reminderId))

            loadReminders()

            showLoading.value = false
        }
    }

    /**
     * id of restored reminder is re-generated
     */
    fun restoreDeletedReminder(deletedReminder: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            val reminder = ReminderDTO(
                deletedReminder.title!!,
                deletedReminder.description,
                deletedReminder.location,
                deletedReminder.latitude!!,
                deletedReminder.longitude!!
            )
            dataSource.saveReminder(reminder)

            val geofenceClient = GeofenceClient(app)
            geofenceClient.addGeofence(reminder.id, reminder.latitude, reminder.longitude)

            loadReminders()

            showLoading.value = false
        }
    }
}