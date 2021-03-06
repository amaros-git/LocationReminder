package com.udacity.location_reminder.locationreminders.reminderslist

import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.BaseRecyclerViewAdapter


//Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {

    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}
