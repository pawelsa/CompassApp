package pl.pawel.compass.compass

import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import pl.pawel.compass.R
import pl.pawel.compass.databinding.CompassFragmentBinding

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CompassFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.rotation.observe(viewLifecycleOwner) {
            Log.d("CompassFragment", "onViewCreated: rotation: $it")
            view.findViewById<TextView>(R.id.message).rotation = -it
        }
    }

    override fun onResume() {
        super.onResume()
        if (haveCompassRequiredSensors()) {
            registerCompassListener()
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

}