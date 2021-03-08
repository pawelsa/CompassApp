package pl.pawel.compass.screen.compass

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pl.pawel.compass.R
import pl.pawel.compass.databinding.CompassFragmentBinding
import pl.pawel.compass.screen.compass.GetLocationDialog.showDialogToSelectDestination
import pl.pawel.compass.services.LocationService
import pl.pawel.compass.utils.PermissionUtils
import pl.pawel.compass.utils.distanceToString

@AndroidEntryPoint
class CompassFragment : Fragment() {
    private lateinit var binding: CompassFragmentBinding
    private val viewModel: CompassViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                setupLocalizationIfTurnedOnOrAskToEnable()
            } else {
                showSnackbarToOpenSettings()
            }
        }

    private var service: LocationService? = null
    private var bound = false
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
            val binder: LocationService.LocationBinder =
                iBinder as LocationService.LocationBinder
            service = binder.service
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            bound = false
        }
    }


    private fun showSnackbarToOpenSettings() {
        Snackbar.make(
            requireView(),
            getString(R.string.location_permission_is_required),
            Snackbar.LENGTH_LONG
        ).setAction(getString(R.string.settings)) {
            startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            })
        }.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CompassFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner, this::handleStateUpdate)

        binding.message.setOnClickListener {
            if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
                setupLocalizationIfTurnedOnOrAskToEnable()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun handleStateUpdate(state: CompassState) {
        return when (state) {
            is CompassState.OnlyCompass -> updateCompassRotation(state.bearing)
            is CompassState.CompassWithLocalizationState -> updateCompassWithLocalization(state)
        }
    }

    private fun updateCompassWithLocalization(state: CompassState.CompassWithLocalizationState) =
        with(binding) {
            message.text = resources.getString(
                R.string.distance_left,
                state.distanceToDestination.distanceToString()
            )
            compassView.northAngle = state.bearing
            compassView.destinationAngle = state.bearingOfDestination
        }

    private fun updateCompassRotation(bearing: Float) = with(binding) {
        compassView.northAngle = bearing
    }

    private fun setupLocalizationIfTurnedOnOrAskToEnable() {
        if (PermissionUtils.isLocationEnabled(requireContext())) {
            setUpLocationListener()
            showDialogToSelectDestination(layoutInflater, viewModel::updateDestination)
        } else {
            viewModel.shouldStartGettingLocalization = true
            PermissionUtils.showEnableGPSDialog(requireContext()) {
                viewModel.shouldStartGettingLocalization = false
            }
        }
    }

    private fun setUpLocationListener() {
        requireActivity().bindService(
            Intent(context, LocationService::class.java),
            serviceConnection,
            Context.BIND_AUTO_CREATE
        )
        viewModel.startObservingLocation()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startObservingCompass()
        if (viewModel.shouldStartGettingLocalization) {
            setupLocalizationIfTurnedOnOrAskToEnable()
        }
    }


    override fun onPause() {
        viewModel.stopObserving()
        if (viewModel.shouldStartGettingLocalization && bound) {
            requireActivity().unbindService(serviceConnection)
        }
        super.onPause()
    }

    private fun showSnackbarThatAppRequiresGpsToWork() {
        Snackbar.make(
            requireView(),
            getString(R.string.required_for_this_app),
            Snackbar.LENGTH_LONG
        ).show()
    }

}