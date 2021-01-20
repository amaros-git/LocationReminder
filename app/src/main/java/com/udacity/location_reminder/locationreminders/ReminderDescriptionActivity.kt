package com.udacity.location_reminder.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminder.R
import com.udacity.location_reminder.ReminderDetailsView
import com.udacity.location_reminder.databinding.ActivityReminderDescriptionBinding
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.android.synthetic.main.it_reminder.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private val TAG = ReminderDescriptionActivity::class.java.simpleName

    private val repository: ReminderDataSource by inject()

    companion object {
        private const val EXTRA_ListOfReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminders: ArrayList<ReminderDataItem>): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            //intent.putExtra(EXTRA_ReminderDataItem, reminders)
            intent.putParcelableArrayListExtra(EXTRA_ListOfReminderDataItem, reminders)
            return intent
        }
    }

    private lateinit var binding: ActivityReminderDescriptionBinding

    private lateinit var reminders: ArrayList<ReminderDataItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_reminder_description
        )

        reminders = intent.extras?.get(EXTRA_ListOfReminderDataItem) as ArrayList<ReminderDataItem>
        Log.d(TAG, "received reminders")
        reminders.forEach {
            Log.d(TAG, it.toString())
        }

        title = getString(R.string.activity_details_title)

        for (i: Int in reminders.indices) {
            val view = ReminderDetailsView(applicationContext)
            view.findViewById<TextView>(R.id.title).text = reminders[i].title
            view.findViewById<TextView>(R.id.description).text = reminders[i].description
            view.findViewById<TextView>(R.id.location).text = reminders[i].location

            binding.remindersList.addView(view)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        GlobalScope.launch(Dispatchers.IO) {
            //remi
        }
    }
}
