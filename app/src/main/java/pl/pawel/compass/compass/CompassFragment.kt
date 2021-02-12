package pl.pawel.compass.compass

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
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
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import pl.pawel.compass.R
import pl.pawel.compass.data.Location
import pl.pawel.compass.databinding.CompassFragmentBinding
import pl.pawel.compass.utils.CalculateBearing
import pl.pawel.compass.utils.PermissionUtils

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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                setupLocalizationIfTurnedOnOrAskToEnable()
            } else {
                Snackbar.make(
                    requireView(),
                    "This permission is required for this function",
                    Snackbar.LENGTH_LONG
                ).setAction("Settings") {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }.show()
            }
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
//            Log.d("CompassFragment", "onViewCreated: rotation: $it")
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
            Toast.makeText(
                requireContext(),
                "Now I should ask you to select destination",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            viewModel.shouldStartGettingLocalization = true
            viewModel.shouldShowDialogToChooseDestination = true
            PermissionUtils.showGPSNotEnabledDialog(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        if (haveCompassRequiredSensors()) {
            registerCompassListener()
        }
        if (viewModel.shouldStartGettingLocalization) {
            if (PermissionUtils.isLocationEnabled(requireContext())) {
                setUpLocationListener()
                if (viewModel.shouldShowDialogToChooseDestination) {
                    Toast.makeText(
                        requireContext(),
                        "Now I should ask you to select destination",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                showSnackbarThatRequiresGpsForAppToWork()
            }
        }
    }

    override fun onPause() {
        sensorManager.unregisterListener(compassListener)
        super.onPause()
    }

    private fun registerCompassListener() {
        sensorManager.apply {
            registerListener(
                compassListener, getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
            )
            registerListener(
                compassListener, getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    private fun haveCompassRequiredSensors(): Boolean {
        val compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        val accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        return compassSensor != null && accelerometerSensor != null
    }

    @SuppressLint("MissingPermission")
    private fun setUpLocationListener() {
        val fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(activity as AppCompatActivity)
        val locationRequest = LocationRequest().setInterval(5000).setFastestInterval(2000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationRequestBuilder =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

        LocationServices.getSettingsClient(activity as AppCompatActivity)
            .checkLocationSettings(locationRequestBuilder.build())
            .addOnSuccessListener {
                fusedLocationProviderClient.requestLocationUpdates(
                    locationRequest,
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            for (location in locationResult.locations) {
                                Log.d(
                                    "CompassFragment",
                                    "onLocationResult: ${location.latitude}, ${location.longitude}"
                                )
                                val second = Location(52.23451767851094f, 21.011770878906265f)
                                val angle = CalculateBearing.getDegree(
                                    Location(
                                        location.latitude.toFloat(),
                                        location.longitude.toFloat()
                                    ), second
                                )
                                Log.d("CompassFragment", "onLocationResult: angle: $angle")
                            }
                        }
                    },
                    Looper.myLooper()
                )
            }.addOnFailureListener {
                showSnackbarThatRequiresGpsForAppToWork()
            }

    }

    private fun showSnackbarThatRequiresGpsForAppToWork() {
        Snackbar.make(
            requireView(),
            "GPS is required for this functionality to work",
            Snackbar.LENGTH_LONG
        ).show()
    }

}