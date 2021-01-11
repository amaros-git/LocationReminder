package com.udacity.location_reminder

import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView

class HelperViewCreator(private val context: Context) {

    fun createLinerLayout(): LinearLayout {
        return LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            orientation = LinearLayout.VERTICAL
        }
    }

    fun createTextView(text: String): TextView {
        return TextView(context).apply {
            //val typeface = ResourcesCompat.getFont(context, R.font.roboto_light)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(16, 0, 0, 16)
            setLayoutParams(layoutParams)
            this.text = text
            textSize = context.resources.getDimension(R.dimen.text_size_medium)
            //setTypeface(typeface)
        }
    }
}

