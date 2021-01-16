package com.udacity.location_reminder.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.location_reminder.R
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.dto.ReminderDTO
import com.udacity.location_reminder.util.FakeRemindersRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsInstanceOf.instanceOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.koin.test.inject
import org.mockito.Mockito
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

/*    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()*/


    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = ApplicationProvider.getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { FakeRemindersRepository() as ReminderDataSource }
        }

        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @After
    fun clear() {
        stopKoin()

        runBlockingTest {
            repository.deleteAllReminders()
        }
    }

    @Test
    fun clickAddReminder_navigateToSaveReminderFragment() {
        val viewModel: RemindersListViewModel by inject()


        //Start fragment
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = Mockito.mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //Then click on addReminder fab
        Espresso.onView(withId(R.id.addReminderFAB))
            .perform(click())

        //Verify app navigated to the SaveReminderFragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    @Test
    fun addReminder_reminderIsDisplayedInRecyclerView() {
        //Add reminder to the repository
        val reminder = ReminderDTO("Title1", "Description1", "Location1", 1.0, 1.0)
        runBlockingTest {
            repository.saveReminder(reminder)
        }

        //Start fragment
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Verify reminders RecyclerView contains saved reminder
        onView(withText(reminder.title)).check(matches(isDisplayed()))
    }

    @Test
    fun remindersRepositoryIsEmpty_checkIfNoDataViewIsShown() {
        //Start fragment. Because we clear repository in @After, it will be empty
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        //Verify if NO Data Image is shown
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

    }
}

