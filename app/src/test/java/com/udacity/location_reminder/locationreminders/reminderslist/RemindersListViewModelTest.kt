package com.udacity.location_reminder.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.location_reminder.R
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.locationreminders.MainCoroutineRule
import com.udacity.location_reminder.locationreminders.data.FakeRemindersRepository
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.locationreminders.getOrAwaitValue
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
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

    private lateinit var remindersRepository: FakeRemindersRepository

    @Before
    fun setupViewModel() {
        remindersRepository = FakeRemindersRepository()

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
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        //verify showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

        //verify saved in fillDataSource() data are the same as in remindersList LiveData
        assertThat(viewModel.remindersList.getOrAwaitValue().size, `is`(reminderDataItems.size))

        // verify we show actual data
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_localDataSourceDoesNOTHaveData() {
        remindersRepository.setReturnError(true)

        mainCoroutineRule.pauseDispatcher()

        viewModel.loadReminders()

        //verify showLoading is true
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        // Execute pending coroutines actions.
        mainCoroutineRule.resumeDispatcher()

        //verify showLoading is false
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))

        //Verify no data was set to remindersList, si its value shall be null
        assertThat(viewModel.remindersList.value, `is`(nullValue()))

        // verify ReminderListFragment shows No data image
        assertThat(viewModel.showNoData.getOrAwaitValue(), `is`(true))

        //Verify snackbar shows ResultError test text
        assertThat(viewModel.showSnackBar.getOrAwaitValue(), `is`("Test error"))
    }

    //We don't need separate test for invalidateShowNoData(), it is checked
    //in the tests of loadReminders()
    /*@Test
    fun invalidateShowNoData_invalidData() {}*/

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