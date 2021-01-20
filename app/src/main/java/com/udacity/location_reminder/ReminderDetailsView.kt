package com.udacity.location_reminder

import android.content.Context
import android.util.AttributeSet
import androidx.cardview.widget.CardView

class ReminderDetailsView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CardView(context, attrs) {

    init {
        inflate(context, R.layout.reminder_triggered, this)
    }
}