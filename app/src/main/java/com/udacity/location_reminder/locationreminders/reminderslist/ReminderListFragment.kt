package com.udacity.location_reminder.locationreminders.reminderslist

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.material.snackbar.Snackbar
import com.udacity.location_reminder.R
import com.udacity.location_reminder.authentication.AuthenticationActivity
import com.udacity.location_reminder.base.BaseFragment
import com.udacity.location_reminder.base.NavigationCommand
import com.udacity.location_reminder.databinding.FragmentRemindersBinding
import com.udacity.location_reminder.locationreminders.RemindersActivity
import com.udacity.location_reminder.locationreminders.geofence.GeofenceClient
import com.udacity.location_reminder.utils.setDisplayHomeAsUpEnabled
import com.udacity.location_reminder.utils.setTitle
import com.udacity.location_reminder.utils.setup
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {

    private val TAG = ReminderListFragment::class.java.simpleName

    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentRemindersBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener {
            _viewModel.loadReminders()
            if (binding.refreshLayout.isRefreshing) {
                binding.refreshLayout.isRefreshing = false;
            }
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        checkIfLocationIsEnabled()
    }

    private fun checkIfLocationIsEnabled(resolve: Boolean = true) {
        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)

        val settingsClient = LocationServices.getSettingsClient(requireActivity())
        val locationSettingsResponseTask =
            settingsClient.checkLocationSettings(builder.build())

        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(
                        activity,
                        REQUEST_TURN_DEVICE_LOCATION_ON
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(TAG, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                    binding.root,
                    R.string.location_required_error, Snackbar.LENGTH_SHORT
                ).setAction(android.R.string.ok) {
                    checkIfLocationIsEnabled(false)
                }.show()
            }
        }
       /* locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) { //TODO what next

            }
        }*/
    }

    //TODO
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkIfLocationIsEnabled(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            Log.d(TAG, "Add button clicked")
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
       /* //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )*/
        findNavController().navigate(ReminderListFragmentDirections.toSaveReminder())
        /*_viewModel.navigationCommand.value = NavigationCommand.To(
            ReminderListFragmentDirections.toSaveReminder()
        )*/
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
        }

//        setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d("RemindersActivity", "onOptionsItemSelected called")
        when (item.itemId) {
            R.id.logout -> {
                Log.d(TAG, "Logging out")
                AuthUI.getInstance()
                    .signOut(requireContext())
                    .addOnCompleteListener {
                        val intent = Intent(requireContext(), AuthenticationActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                    }
                return true
            }
            R.id.clearAll -> {
                val result = GlobalScope.async {
                    _viewModel.deleteAllReminders()
                    val geofenceClient = GeofenceClient(requireActivity().application)
                    geofenceClient.removeAllGeofences()
                }

                GlobalScope.launch(Dispatchers.Main) {
                    result.await()
                    //delay(100) //Well, many changes are required to refresh recycler view. TODO REMOVE THIS dirty hacks
                    //binding.refreshLayout.isRefreshing = true
                    _viewModel.loadReminders()
                    //binding.refreshLayout.isRefreshing = false
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }
}

    private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
