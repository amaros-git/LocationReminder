package com.udacity.location_reminder.locationreminders

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.udacity.location_reminder.R
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.local.LocalDB
import com.udacity.location_reminder.locationreminders.data.local.RemindersLocalRepository
import kotlinx.android.synthetic.main.activity_reminders.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private val repository: ReminderDataSource by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminders)

        val remindersLocalRepository: ReminderDataSource by inject()
        GlobalScope.launch {
            remindersLocalRepository.deleteAllReminders()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("RemindersActivity", "onOptionsItemSelected called")
        when (item.itemId) {
            android.R.id.home -> {
                findNavController(R.id.nav_host_fragment).popBackStack()
                return true
            }
            R.id.clearAll -> {
               val db = LocalDB.createRemindersDao(this)
               GlobalScope.launch {
                   repository.deleteAllReminders()
               }
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
