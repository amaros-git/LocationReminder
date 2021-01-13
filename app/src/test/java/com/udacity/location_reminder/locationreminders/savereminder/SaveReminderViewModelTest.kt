package com.udacity.location_reminder.locationreminders.savereminder

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.base.CharMatcher.isNot
import com.udacity.location_reminder.R
import com.udacity.location_reminder.locationreminders.MainCoroutineRule
import com.udacity.location_reminder.locationreminders.data.FakeDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.getOrAwaitValue
import com.udacity.location_reminder.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
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

    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()

        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersRepository
        )
    }


    @Test
    fun saveReminder_saveAndCheckToastNavigationCommand() {
        // Pause dispatcher so you can verify initial values.
        mainCoroutineRule.pauseDispatcher()

        // then save reminder
        val reminder = ReminderDataItem("Title", "Description", "Location", 1.0, 1.0)
        viewModel.saveReminderAndNavigateBack(reminder)

        //verify showLoading is true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()


        //verify showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

        //verify Toast message
        val toastText = viewModel.showToast.getOrAwaitValue()
        val expectedString =
            ApplicationProvider.getApplicationContext<Application>().getString(R.string.reminder_saved)
        assertThat(toastText, `is`(expectedString))
    }

    //TODO: provide testing to the SaveReminderView and its live data objects


}