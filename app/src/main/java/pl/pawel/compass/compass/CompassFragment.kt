package pl.pawel.compass.compass

import android.Manifest
import android.content.Intent
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.snackbar.Snackbar
import pl.pawel.compass.R
import pl.pawel.compass.databinding.CompassFragmentBinding
import pl.pawel.compass.utils.PermissionUtils
import pl.pawel.compass.utils.haveCompassRequiredSensors
import pl.pawel.compass.utils.registerCompassListener

class CompassFragment : Fragment() {
    private lateinit var binding: CompassFragmentBinding
    private val viewModel: CompassViewModel by viewModels()
    private val sensorManager: SensorManager by lazy {
        getSystemService(requireContext(), SensorManager::class.java) as SensorManager
    }
    private val compassListener: CompassListener by lazy {
        CompassListener { rotation ->
            viewModel.updateRotation(rotation)
        }
    }
    private val locationListener: LocationListener by lazy {
        LocationListener(activity as AppCompatActivity) {
            viewModel.updateLocation(it)
        }
    }

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

//        val first = Localization(49.98303370984737f, 18.94222131835939f)
//        val second = Localization(52.23451767851094f, 21.011770878906265f)
//        val angle = CalculateBearing.getDegree(first, second)

        viewModel.rotation.observe(viewLifecycleOwner) {
            view.findViewById<TextView>(R.id.message).rotation = -it
        }
        view.findViewById<TextView>(R.id.message).setOnClickListener {
            if (PermissionUtils.isAccessFineLocationGranted(requireContext())) {
                setupLocalizationIfTurnedOnOrAskToEnable()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun setupLocalizationIfTurnedOnOrAskToEnable() {
        if (PermissionUtils.isLocationEnabled(requireContext())) {
            setUpLocationListener()
            showDialogToSelectDestination()
        } else {
            viewModel.shouldStartGettingLocalization = true
            PermissionUtils.showEnableGPSDialog(requireContext())
        }
    }

    private fun setUpLocationListener() {
        locationListener.startObservingLocation(
            activity as AppCompatActivity,
            onError = {
                showSnackbarThatAppRequiresGpsToWork()
            })
    }

    override fun onResume() {
        super.onResume()
        if (sensorManager.haveCompassRequiredSensors()) {
            sensorManager.registerCompassListener(compassListener)
        }
        if (viewModel.shouldStartGettingLocalization) {
            if (PermissionUtils.isLocationEnabled(requireContext())) {
                setUpLocationListener()
                showDialogToSelectDestination()
            } else {
                showSnackbarThatAppRequiresGpsToWork()
            }
        }
    }

    private fun showDialogToSelectDestination() {
        Toast.makeText(
            requireContext(),
            "Now I should ask you to select destination",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onPause() {
        sensorManager.unregisterListener(compassListener)
        if (viewModel.shouldStartGettingLocalization) {
            locationListener.stopObtainingLocation()
        }
        super.onPause()
    }

    private fun showSnackbarThatAppRequiresGpsToWork() {
        Snackbar.make(
            requireView(),
            getString(R.string.gps_required),
            Snackbar.LENGTH_LONG
        ).show()
    }

}