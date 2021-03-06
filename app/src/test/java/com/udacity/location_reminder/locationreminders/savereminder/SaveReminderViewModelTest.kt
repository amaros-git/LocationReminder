package com.udacity.location_reminder.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.locationreminders.MainCoroutineRule
import com.udacity.location_reminder.locationreminders.data.FakeRemindersRepository
import com.udacity.location_reminder.locationreminders.getOrAwaitValue
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: SaveReminderViewModel

    private lateinit var remindersRepository: FakeRemindersRepository

    @Before
    fun setupViewModel() {
        remindersRepository = FakeRemindersRepository()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersRepository
        )
    }

    @After
    fun clear() {
        stopKoin()
    }

    @Test
    fun saveReminder_saveAndCheckToastNavigationCommand() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // then save reminder
        val reminder = ReminderDataItem("Title", "Description", "Location", 1.0, 1.0)
        viewModel.saveReminder(reminder)

        //verify showLoading is true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()


        //verify showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun validateEnteredData_reminderContainsError() {
        //when reminder contains error
        val reminderLocationNull = ReminderDataItem("Title", "Description", null, 1.0, 1.0)
        val reminderTitleEmpty = ReminderDataItem("", "Description", "Location", 1.0, 1.0)

        //verify error type and false return if location is empty
        assertThat(viewModel.validateEnteredData(reminderLocationNull), `is`(false))
        val errorLocation =
            ApplicationProvider.getApplicationContext<Application>().getString(R.string.err_select_location)
        assertThat(viewModel.showErrorMessage.getOrAwaitValue(), `is`(errorLocation))

        //verify error type and false return if title is empty
        assertThat(viewModel.validateEnteredData(reminderTitleEmpty), `is`(false))
        val errorTitle =
            ApplicationProvider.getApplicationContext<Application>().getString(R.string.err_enter_title)
        assertThat(viewModel.showErrorMessage.getOrAwaitValue(), `is`(errorTitle))
    }
}