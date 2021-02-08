package com.udacity.location_reminder

import android.app.Application
import androidx.databinding.adapters.TextViewBindingAdapter
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.location_reminder.locationreminders.RemindersActivity
import com.udacity.location_reminder.locationreminders.data.ReminderDataSource
import com.udacity.location_reminder.locationreminders.data.local.LocalDB
import com.udacity.location_reminder.locationreminders.data.local.RemindersLocalRepository
import com.udacity.location_reminder.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.location_reminder.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.location_reminder.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.not
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
import java.lang.Thread.sleep

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :

    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource

    private lateinit var appContext: Application

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin

        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
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
    fun clean() {
        stopKoin()
        runBlocking {
            repository.deleteAllReminders()
        }

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregister() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
    }

    /**
     * Big tests which check addition and deletion of reminder and all related steps
     */
    @Test
    fun addAndDeleteReminder() {
        //On the start repository must be empty.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)

        //Verify if No Data ImageView is visible
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        //Click on add reminder and go to SaveReminderFragment
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Verify views are empty initially
        onView(withId(R.id.selectedLocation)).check(matches(withText("")))
        onView(withId(R.id.reminderTitle)).check(matches(withText("")))
        onView(withId(R.id.reminderDescription)).check(matches(withText("")))

        //Click on select location
        onView(withId(R.id.selectLocation)).perform(click())

        //Add random marker on the map
        onView(withId(R.id.map)).perform(longClick())

        //Click on save location button
        onView(withId(R.id.save_location_button)).perform(click())

        //Enter title and description
        val title = "Title1"
        val description = "Description1"
        onView(withId(R.id.reminderTitle)).perform(typeText(title))
        onView(withId(R.id.reminderDescription))
            .perform(typeText(description))
            .perform(closeSoftKeyboard());

        //Save reminder
        onView(withId(R.id.saveReminder)).perform(click())

        //Verify title and description in Recycler view
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        //swipe left to delete
        onView(withText(title)).perform(swipeLeft())

        //check no data is displayed because there was only one reminder
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

        activityScenario.close()
    }
}
