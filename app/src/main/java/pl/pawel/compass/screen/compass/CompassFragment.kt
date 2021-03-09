package pl.pawel.compass.screen.compass

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import pl.pawel.compass.R
import pl.pawel.compass.base.BaseFragmentWithService
import pl.pawel.compass.databinding.CompassFragmentBinding
import pl.pawel.compass.dialogs.EnableGpsDialog.showEnableGPSDialog
import pl.pawel.compass.dialogs.GetLocationDialog.showDialogToSelectDestination
import pl.pawel.compass.services.LocationService
import pl.pawel.compass.utils.PermissionUtils
import pl.pawel.compass.utils.distanceToString

@AndroidEntryPoint
class CompassFragment : BaseFragmentWithService<LocationService>() {
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

    private fun handleStateUpdate(state: CompassViewModel.ScreenState) {
        return when (state) {
            is CompassViewModel.ScreenState.OnlyCompass -> updateCompassRotation(state.bearing)
            is CompassViewModel.ScreenState.CompassWithLocalization -> updateCompassWithLocalization(state)
        }
    }

    private fun updateCompassWithLocalization(state: CompassViewModel.ScreenState.CompassWithLocalization) =
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
            showDialogToSelectDestination(layoutInflater, viewModel::startObserving)
        } else {
            viewModel.shouldStartGettingLocalization = true
            showEnableGPSDialog(requireContext()) {
                viewModel.shouldStartGettingLocalization = false
            }
        }
    }

    private fun setUpLocationListener() {
        requireActivity().bindService(
                Intent(context, LocationService::class.java),
                serviceBinder,
                Context.BIND_AUTO_CREATE
        )
        viewModel.startObserving()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startObserving()
        if (viewModel.shouldStartGettingLocalization) {
            setupLocalizationIfTurnedOnOrAskToEnable()
        }
    }


    override fun onPause() {
        viewModel.stopObserving()
        if (viewModel.shouldStartGettingLocalization && service != null) {
            requireActivity().unbindService(serviceBinder)
        }
        super.onPause()
    }

}