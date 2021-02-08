package com.udacity.location_reminder.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminder.R
import com.udacity.location_reminder.ReminderDetailsView
import com.udacity.location_reminder.databinding.ActivityReminderDescriptionBinding
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.dto.Result
import com.udacity.location_reminder.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.location_reminder.locationreminders.geofence.GeofenceClient
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import com.udacity.location_reminder.utils.fadeOut
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.coroutines.CoroutineContext

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private val TAG = ReminderDescriptionActivity::class.java.simpleName

    private var coroutineJob: Job = Job()
    private val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + coroutineJob

    private val repository: ReminderDataSource by inject()

    companion object {
        private const val EXTRA_ListOfReminderDataItem = "EXTRA_ReminderDataItem"
        private const val EXTRA_GeofenceError = "EXTRA_GeofenceError"

        fun newIntent(context: Context, result: Result<ArrayList<ReminderDataItem>>): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            if (result is Result.Success) {
                intent.putParcelableArrayListExtra(EXTRA_ListOfReminderDataItem, result.data)
            } else {
                intent.putExtra(EXTRA_GeofenceError, (result as Result.Error).message)
            }
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        //check if we have geofence error
        val geofenceError = intent.extras?.get(EXTRA_GeofenceError) as String?
        geofenceError?.let {
            showError(it)
        }

        //check if we have reminders
        val reminders =
            intent.extras?.get(EXTRA_ListOfReminderDataItem) as ArrayList<ReminderDataItem>?
        if (null != reminders) {
            title = getString(R.string.activity_details_title)
            showReminders(reminders)
        } else {
            val error = "No reminders received"
            Log.d(TAG, error)
            showError(error)
        }
    }

    /**
     * Creates custom Card View, fills with data and sets onClick listeners
     * for views show map and remove reminder
     */
    private fun showReminders(reminders: ArrayList<ReminderDataItem>) {
        for (i: Int in reminders.indices) {
            val view = createReminderView(reminders[i])

            view.findViewById<TextView>(R.id.RemoveReminderButton).setOnClickListener {
                removeReminder(reminders[i].id)
                view.fadeOut() //remove reminder view
            }

            view.findViewById<TextView>(R.id.showOnMapButton).setOnClickListener {
                Toast.makeText(applicationContext, "Not implemented, sorry ;)", Toast.LENGTH_SHORT)
                    .show()
            }

            binding.remindersList.addView(view)
        }
    }

    private fun createReminderView(reminder: ReminderDataItem) =
        ReminderDetailsView(applicationContext).apply {
            findViewById<TextView>(R.id.title).text = reminder.title
            findViewById<TextView>(R.id.description).text = reminder.description
            findViewById<TextView>(R.id.location).text = reminder.location
        }


    private fun removeReminder(reminderId: String) {
        CoroutineScope(coroutineContext).launch {
            repository.deleteReminder(reminderId)
            val geofenceClient = GeofenceClient(application)
            geofenceClient.removeGeofences(listOf(reminderId))
        }
    }

    private fun showError(error: String) {
        Toast.makeText(applicationContext, error, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineJob.cancel("Activity destroyed")
    }
}
