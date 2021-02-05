package com.udacity.location_reminder.locationreminders.geofence



import androidx.core.app.JobIntentService

import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.data.dto.Result

import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.location_reminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentCallbacks
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.udacity.location_reminder.R
import com.udacity.location_reminder.locationreminders.data.local.RemindersLocalRepository
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.android.inject

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = GeofenceBroadcastReceiver::class.java.simpleName

    companion object {
        const val GEOFENCE_ERROR_EXTRA = "geofenceError"
    }


    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent.hasError()) {
            val errorString = errorMessage(context, geofencingEvent.errorCode)
            Log.e(TAG, "geofence error [${geofencingEvent.errorCode}]: $errorString")

            intent.putExtra(GEOFENCE_ERROR_EXTRA, errorString)
        }

        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }

    /**
     * Returns the error string for a geofencing error code.
     */
    private fun errorMessage(context: Context, errorCode: Int): String {
        val resources = context.resources
        return when (errorCode) {
            GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> resources.getString(
                R.string.geofence_not_available
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> resources.getString(
                R.string.geofence_too_many_geofences
            )
            GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> resources.getString(
                R.string.geofence_too_many_pending_intents
            )
            else -> resources.getString(R.string.unknown_geofence_error)
        }
    }
}