package com.udacity.location_reminder.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.locationreminders.MainCoroutineRule
import com.udacity.location_reminder.locationreminders.data.FakeDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.getOrAwaitValue
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var viewModel: RemindersListViewModel

    private lateinit var remindersRepository: FakeDataSource

    @Before
    fun setupViewModel() {
        remindersRepository = FakeDataSource()

        viewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            remindersRepository
        )
    }

    @After
    fun clear() {
        stopKoin()

        runBlockingTest {
            remindersRepository.deleteAllReminders()
        }
    }

    @Test
    fun loadReminders_localDataSourceHasData() {
        mainCoroutineRule.pauseDispatcher()

        val reminderDataItems = fillDataSource()

        viewModel.loadReminders()

        //verify showLoading is true
        MatcherAssert.assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()


        //verify showLoading is false
        MatcherAssert.assertThat(
            viewModel.remindersList.getOrAwaitValue().size, `is`(reminderDataItems.size)
        )

       /* //verify Toast message
        val toastText = viewModel.showToast.getOrAwaitValue()
        val expectedString =
            ApplicationProvider.getApplicationContext<Application>()
                .getString(R.string.reminder_saved)
        MatcherAssert.assertThat(toastText, Is.`is`(expectedString))

        //verify Navigation command
        MatcherAssert.assertThat(viewModel.navigationCommand.value, Is.`is`(NavigationCommand.Back))*/


    }

    @Test
    fun loadReminders_localDataSourceDoesNOTHaveData() {

    }

    @Test
    fun invalidateShowNoData_invalidData() {

    }

    @Test
    fun invalidateShowNoData_validData() {

    }

    private fun fillDataSource(): List<ReminderDataItem> {
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
            remindersRepository.saveReminder(reminder1)
            remindersRepository.saveReminder(reminder2)
            remindersRepository.saveReminder(reminder3)
        }

        return listOf(reminder1, reminder2, reminder3).map { reminder ->
            ReminderDataItem(
                reminder.title,
                reminder.description,
                reminder.location,
                reminder.latitude,
                reminder.longitude,
                reminder.id
            )
        }
    }

}