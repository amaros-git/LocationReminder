package com.udacity.location_reminder.locationreminders

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.databinding.DataBindingUtil
import com.udacity.location_reminder.R
import com.udacity.location_reminder.ReminderCardView
import com.udacity.location_reminder.databinding.ActivityReminderDescriptionBinding
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem
import kotlinx.android.synthetic.main.it_reminder.*

/**
 * Activity that displays the reminder details after the user clicks on the notification
 */
class ReminderDescriptionActivity : AppCompatActivity() {

    private val TAG = ReminderDescriptionActivity::class.java.simpleName

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

        val card = ReminderCardView(this)

        card.findViewById<TextView>(R.id.huj_text).text = "Param"
        card.findViewById<TextView>(R.id.pizda_text).text = "Pam Pam"

        val card2 = ReminderCardView(this)

        card2.findViewById<TextView>(R.id.huj_text).text = "Param"
        card2.findViewById<TextView>(R.id.pizda_text).text = "Pam Pam"


        reminder?.let {
            binding.remindersList.addView(card)
            binding.remindersList.addView(card2)
        }

    }

    /*private fun createReminderCardView(reminder: ReminderDataItem): CardView {
        val helper = HelperViewCreator(this)

        val remindersLayout = helper.createLinerLayout()
        remindersLayout.apply {
            reminder.title?.let {
                addView(helper.createTextView(it))
            }
            reminder.description?.let {
                addView(helper.createTextView(it))
            }
            reminder.location?.let {
                addView(helper.createTextView(it))
            }
            addView(helper.createTextView(reminder.latitude.toString()))
            addView(helper.createTextView(reminder.longitude.toString()))
        }

        return CardView(this).apply {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 0, 40)
            setLayoutParams(layoutParams)
            addView(remindersLayout)
        }
    }*/
}
