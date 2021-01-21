package com.udacity.location_reminder.locationreminders.data

import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.data.dto.Result

class FakeRemindersRepository : ReminderDataSource {

    private val reminders = LinkedHashMap<String, ReminderDTO>()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return when (shouldReturnError) {
            false -> {
                Result.Success(reminders.values.toList())
            }

            true -> {
                Result.Error("Test error")
            }
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = reminders[id] ?: return Result.Error("Test error")
        return Result.Success(reminder)
    }

    override suspend fun deleteAllReminders() {
       reminders.clear()
    }

    override suspend fun deleteReminder(id: String) {
        reminders.remove(id)
    }
}