package com.udacity.location_reminder.locationreminders.geofence

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.JobIntentService
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.data.dto.Result
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.location_reminder.utils.sendNotification
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

class GeofenceTransitionsJobIntentService : JobIntentService(), CoroutineScope {

    private val TAG = GeofenceTransitionsJobIntentService::class.java.simpleName

    private var coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    companion object {
        private const val JOB_ID = 573

        fun enqueueWork(context: Context, intent: Intent) {

            enqueueWork(
                context,
                GeofenceTransitionsJobIntentService::class.java, JOB_ID,
                intent
            )
        }
    }

    override fun onHandleWork(intent: Intent) {
        if (intent.action == GeofenceClient.ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            val geofenceError =
                intent.extras?.get(EXTRA_GeofenceError) as String?
            geofenceError?.let {
                sendNotification(this@GeofenceTransitionsJobIntentService, Result.Error(it))
                return
            }
            //else
            if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                val geofences = when {
                    geofencingEvent.triggeringGeofences.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences
                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found!")
                        return
                    }
                }
                CoroutineScope(coroutineContext).launch(SupervisorJob()) {
                    val reminders: ArrayList<ReminderDataItem> = getReminders(geofences)
                    sendNotification(
                        this@GeofenceTransitionsJobIntentService,
                        Result.Success(reminders)
                    )
                }
            }
        }
    }

    private suspend fun getReminders(triggeringGeofences: List<Geofence>): ArrayList<ReminderDataItem> {
        val repository: ReminderDataSource by inject()

        return withContext(Dispatchers.IO) {
            val triggeredReminders = ArrayList<ReminderDataItem>()
            for (i: Int in triggeringGeofences.indices) {
                val result = repository.getReminder(triggeringGeofences[i].requestId)
                if (result is Result.Success<ReminderDTO>) {
                    triggeredReminders.add(
                        ReminderDataItem(
                            result.data.title,
                            result.data.description,
                            result.data.location,
                            result.data.latitude,
                            result.data.longitude,
                            result.data.id
                        )
                    )
                }
            }

            return@withContext triggeredReminders
        }
    }
}