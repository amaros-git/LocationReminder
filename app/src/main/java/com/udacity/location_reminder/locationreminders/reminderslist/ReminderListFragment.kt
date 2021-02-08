package com.udacity.location_reminder.locationreminders.reminderslist

import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
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
import com.udacity.location_reminder.utils.setDisplayHomeAsUpEnabled
import com.udacity.location_reminder.utils.setTitle
import com.udacity.location_reminder.utils.setup
import org.koin.androidx.viewmodel.ext.android.viewModel


class ReminderListFragment : BaseFragment() {

    private val TAG = ReminderListFragment::class.java.simpleName

    override val _viewModel: RemindersListViewModel by viewModel()

    private lateinit var binding: FragmentRemindersBinding

    private lateinit var adapterReminders: RemindersListAdapter

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
        binding.lifecycleOwner = this

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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_TURN_DEVICE_LOCATION_ON) {
            checkIfLocationIsEnabled(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()

        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        adapterReminders = RemindersListAdapter {
            Log.d(TAG, "callback $it")
        }

        binding.remindersRecyclerView.setup(adapterReminders)

        val itemTouchCallback = ItemTouchHelper(reminderListItemTouchCallback)
        itemTouchCallback.attachToRecyclerView(binding.remindersRecyclerView)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
                _viewModel.deleteAllReminders()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.main_menu, menu)
    }

    private var reminderListItemTouchCallback: ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                val position = viewHolder.adapterPosition
                val reminder = adapterReminders.getItem(position)

                _viewModel.deleteReminder(reminder.id)

                Snackbar.make(
                    binding.root,
                    requireContext().getString(R.string.restore_reminder),
                    Snackbar.LENGTH_LONG
                ).apply {
                    setAction(R.string.undo) {
                        restoreDeletedReminder(reminder)
                    }

                    show()
                }
            }
        }

    private fun restoreDeletedReminder(deletedReminder: ReminderDataItem) {
        _viewModel.restoreDeletedReminder(deletedReminder)
    }
}

private const val REQUEST_TURN_DEVICE_LOCATION_ON = 29
