package com.udacity.location_reminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    /**
     * This and the next getReminderById methods test both DAO APIs:
     * saveReminder and getReminder(s)
     */
    @Test
    fun getReminders_SaveAndGetRemindersSuccessfully() = runBlockingTest {
        // Load test reminder to the database
        val savedReminders = loadTestRemindersToDatabase()

        //Then get reminders from the database
        val loadedReminders = database.reminderDao().getReminders()

        //Verify count is the same
        assertThat(savedReminders.size, `is`(loadedReminders.size))
    }

    @Test
    fun getReminderById_SaveAndGetReminderSuccessfully() = runBlockingTest {
        // Load test reminder to the database
        val testReminder1 = ReminderDTO(
            "Title1", "Description1", "Location1",
            1.0, 1.0
        )
        database.reminderDao().saveReminder(testReminder1)

        //Then get reminder from the database
        val loadedReminder = database.reminderDao().getReminderById(testReminder1.id)

        //Verify ids are the same
        assertThat(testReminder1.id, `is`(loadedReminder?.id))
        //Verify locations are the same
        assertThat(testReminder1.location, `is`(loadedReminder?.location))
        //Verify latitudes are the same
        assertThat(testReminder1.latitude, `is`(loadedReminder?.latitude))
    }

    @Test
    fun deleteAllReminders_saveRemindersThenDeleteAndGetEmpty() = runBlockingTest {
        // Load test reminders to the database
        loadTestRemindersToDatabase()

        //Then delete all
        database.reminderDao().deleteAllReminders()

        //Then try to load reminders
        val loadedReminders = database.reminderDao().getReminders()

        //Verify database is empty and empty list was returned
        assertThat(loadedReminders, `is`(emptyList()))
    }

    @Test
    fun deleteReminder_addDeleteAndTryToGet() = runBlockingTest {
        //load reminder to the database
        val savedReminder = ReminderDTO(
            "Title1", "Description1", "Location1",
            1.0, 1.0
        )
        database.reminderDao().saveReminder(savedReminder)

        //delete it
        database.reminderDao().deleteReminder(savedReminder.id)

        //try to get deleted reminder
        val loadedReminder = database.reminderDao().getReminderById(savedReminder.id)

        //Verify null is returned if you try to get deleted reminder
        assertThat(loadedReminder, nullValue())

    }



    private fun loadTestRemindersToDatabase(): List<ReminderDTO> {
        val reminder1 = ReminderDTO(
            "Title1", "Description1", "Location1",
            1.0, 1.0
        )
        val reminder2 = ReminderDTO(
            "Title2", "Description2", "Location2",
            1.0, 1.0
        )
        val reminder3 = ReminderDTO(
            "Title3", "Description3", "Location3",
            1.0, 1.0
        )

        runBlockingTest {
            database.reminderDao().saveReminder(reminder1)
            database.reminderDao().saveReminder(reminder2)
            database.reminderDao().saveReminder(reminder3)
        }

        return listOf(reminder1, reminder2, reminder3)
    }

}