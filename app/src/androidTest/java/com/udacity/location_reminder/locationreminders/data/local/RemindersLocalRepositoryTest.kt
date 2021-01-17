package com.udacity.location_reminder.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var repository: RemindersLocalRepository

    private lateinit var database: RemindersDatabase

    @Before
    fun setup() {
        // Using an in-memory database for testing, because it doesn't survive killing the process.
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun cleanUp() {
        database.close()
    }

    // runBlocking is used here because of https://github.com/Kotlin/kotlinx.coroutines/issues/1204
    // TODO: Replace with runBlockingTest once issue is resolved
    @Test
    fun getReminders_loadRemindersAndGetThemSuccessfully() = runBlocking {

        //Load test reminders into database
        val savedReminders = loadTestRemindersToDatabase()

        //Then load reminders from database
        val result = repository.getReminders()

        //Verify Success was returned
        assertThat(result, instanceOf(Result.Success::class.java))

        //Verify size is the same
        result as Result.Success
        assertThat(result.data.size, `is`(savedReminders.size))
    }

     @Test
     fun saveReminder(reminder: ReminderDTO) = runBlockingTest {
         //Save reminder to the database
         val savedReminder = ReminderDTO(
             "Title1", "Description1", "Location1",
             1.0, 1.0
         )
         repository.saveReminder(savedReminder)

         //Then get it from repo
         val loadedReminder = repository.getReminder(savedReminder.id)

         // THEN - Same task is returned.
         assertThat(result.succeeded, Is.`is`(true))
         result as kotlin.Result.Success
         assertThat(result.data.title, Is.`is`("title"))
         assertThat(result.data.description, Is.`is`("description"))
         assertThat(result.data.isCompleted, Is.`is`(false))



     }


     /*@Test
     fun getReminder(id: String): Result<ReminderDTO> {

     }

     @Test
     fun deleteAllReminders() {*/

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
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
            repository.saveReminder(reminder3)
        }

        return listOf(reminder1, reminder2, reminder3)
    }
}