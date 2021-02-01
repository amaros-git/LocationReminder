package com.udacity.location_reminder.locationreminders

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.udacity.location_reminder.R
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.geofence.GeofenceClient
import com.udacity.location_reminder.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val repository: ReminderDataSource by inject()
    private val viewModel: RemindersListViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        /*val reminders = ArrayList<String>()

        reminders.add("Test1")


        Log.d("Test", "${reminders.size}")

        Log.d("Test", reminders[0])*/

        /*Log.d("reminders", "title = ${reminders[0].title}")
        val title = if (reminders.size > 0) "Few reminders triggered" else reminders[0].title
        val location = if (reminders.size > 0) "Few location are near" else reminders[0].location*/
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
