package pl.pawel.compass.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs

class CompassListener(private val updateDegrees: (Float) -> Unit) : SensorEventListener {
    private var rotationMatrix = FloatArray(16)
    private var inclinationMatrix = FloatArray(16)
    private var gravityVector = FloatArray(3)
    private var geomagneticVector = FloatArray(3)
    private var orientationVector = FloatArray(3)

    private var degrees = 0f

    companion object {
        private const val DEGREE_THRESHOLD = 2
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (isEventAndAccuracyNotGoodEnough(event)) return

        handleNewSensorData(event!!)

        val successfullyUpdatedMatrices = SensorManager.getRotationMatrix(
            rotationMatrix, inclinationMatrix,
            gravityVector, geomagneticVector
        )
        if (successfullyUpdatedMatrices) {
            SensorManager.getOrientation(rotationMatrix, orientationVector)
            val newDegrees = Math.toDegrees(orientationVector[0].toDouble()).toFloat()
            if (abs(newDegrees - degrees) > DEGREE_THRESHOLD) {
                degrees = newDegrees
                updateDegrees(degrees)
            }
        }

    }

    private fun handleNewSensorData(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> gravityVector = event.values.clone()
            Sensor.TYPE_MAGNETIC_FIELD -> geomagneticVector = event.values.clone()
        }
    }

    private fun isEventAndAccuracyNotGoodEnough(event: SensorEvent?) =
        event == null || event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}