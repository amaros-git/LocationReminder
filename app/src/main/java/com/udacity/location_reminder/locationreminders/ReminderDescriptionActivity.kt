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
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private val TAG = ReminderDescriptionActivity::class.java.simpleName

    private val repository: ReminderDataSource by inject()

    companion object {
        private const val EXTRA_ReminderDataItem = "EXTRA_ReminderDataItem"

        //        receive the reminder object after the user clicks on the notification
        fun newIntent(context: Context, reminderDataItem: ReminderDataItem): Intent {
            val intent = Intent(context, ReminderDescriptionActivity::class.java)
            intent.putExtra(EXTRA_ReminderDataItem, reminderDataItem)
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

        val reminder: ReminderDataItem? =
            intent.extras?.get(EXTRA_ReminderDataItem) as ReminderDataItem?
        Log.d(TAG, "reminder = $reminder")


        //TODO REMOVE
        val reminders = mutableListOf<ReminderDataItem>()
        for (i: Int in 0..20) {
            val item = ReminderDataItem("Title$i", "Description$i", "Location$i", 0.0, 0.0)
            reminders.add(item)
        }

        for (i: Int in 0 until reminders.size) {
            val view = ReminderDetailsView(applicationContext)
            view.findViewById<TextView>(R.id.titleText).text = reminders[i].title
            view.findViewById<TextView>(R.id.titleText).text = reminders[i].description

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
