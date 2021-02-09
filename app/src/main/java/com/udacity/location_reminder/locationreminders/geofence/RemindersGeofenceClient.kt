package com.udacity.location_reminder.locationreminders.geofence

import android.annotation.SuppressLint
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderFragment
import java.util.concurrent.TimeUnit


private val GEOFENCE_EXPIRATION_IN_MILLISECONDS: Long = TimeUnit.HOURS.toMillis(1)
private const val GEOFENCE_RADIUS_IN_METERS = 100f


class GeofenceClient (private val application: Application) {

    companion object {
        const val ACTION_GEOFENCE_EVENT = "LocationReminder.action.ACTION_GEOFENCE_ACTION"
    }

    private val TAG = GeofenceClient::class.java.simpleName

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(application)

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(application, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // Use FLAG_UPDATE_CURRENT so that you get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(application, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    @SuppressLint("MissingPermission")
    fun addGeofence(id: String, lat: Double, long: Double) {
        // Build the Geofence Object
        val geofence = Geofence.Builder()
            // Set the request ID, string to identify the geofence.
            .setRequestId(id)
            // Set the circular region of this geofence.
            .setCircularRegion(lat, long, GEOFENCE_RADIUS_IN_METERS)
            // Set the expiration duration of the geofence. This geofence gets
            // automatically removed after this period of time.
            .setExpirationDuration(GEOFENCE_EXPIRATION_IN_MILLISECONDS)
            // Set the transition types of interest. Alerts are only generated for these
            // transition. We track entry and exit transitions in this sample.
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        // Build the geofence request
        val geofencingRequest = GeofencingRequest.Builder()
            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            // Add the geofences to be monitored by geofencing service.
            .addGeofence(geofence)
            .build()

        //in case of success we navigate back.
        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Added ${geofence.requestId}")
            }
            addOnFailureListener {
                if ((it.message != null)) {
                    Log.w(TAG, it.message!!)
                }
                Log.e(TAG, "Failed to add geofence with id ${geofence.requestId}")
            }
        }
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Successfully removed all geofences")
            }
            addOnFailureListener {
                Log.d(TAG, "Failed to remove all geofences")
            }
        }
    }

    fun removeGeofences(geofenceRequestIds: List<String>) {
        geofencingClient.removeGeofences(geofenceRequestIds)?.run {
            addOnSuccessListener {
                Log.d(TAG, "Successfully removed all geofences")
            }
            addOnFailureListener {
                Log.d(TAG, "Failed to remove all geofences")
            }
        }
    }
}