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

//    TODO: Add testing implementation to the RemindersDao.kt

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    @Before
    fun initDb() {
        // Using an in-memory database so that the information stored here disappears when the
        // process is killed.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun getReminders_SaveAndGetRemindersSuccessfully() {
        // Load test reminder to the database
        val savedReminders = loadTestRemindersToDatabase()

        //Then get reminders from the database
        var loadedReminders: List<ReminderDTO> ? = null
        runBlockingTest {
            loadedReminders = database.reminderDao().getReminders()
        }

        //Verify count is the same
        assertThat(savedReminders.size, `is`(loadedReminders?.size))
    }

    @Test
    fun getReminderById_SaveAndGetReminderSuccessfully() {
        // Load test reminder to the database
        val testReminder1 = ReminderDTO(
            "Title1", "Description1", "Location1",
            1.0, 1.0
        )
        runBlockingTest {
            database.reminderDao().saveReminder(testReminder1)
        }

        //Then get reminder from the database
        var loadedReminder: ReminderDTO? = null
        runBlockingTest {
            loadedReminder = database.reminderDao().getReminderById(testReminder1.id)
        }

        //Verify ids are the same
        assertThat(testReminder1.id, `is`(loadedReminder?.id))
        //Verify locations are the same
        assertThat(testReminder1.location, `is`(loadedReminder?.location))
        //Verify latitudes are the same
        assertThat(testReminder1.latitude, `is`(loadedReminder?.latitude))
    }

    @Test
    fun deleteAllReminders_saveAndDeleteAndGetEmpty() {
        // Load test reminders to the database
        loadTestRemindersToDatabase()

        //Then delete all
        runBlockingTest {
            database.reminderDao().deleteAllReminders()
        }

        //Then try to load reminders
        var loadedReminders: List<ReminderDTO> ? = null
        runBlockingTest {
            loadedReminders = database.reminderDao().getReminders()
        }

        //Verify database is empty and empty list was returned
        assertThat(loadedReminders, `is`(emptyList()))
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