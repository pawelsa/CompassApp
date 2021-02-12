package pl.pawel.compass.utils

import android.hardware.Sensor
import android.hardware.SensorManager
import pl.pawel.compass.compass.CompassListener


fun SensorManager.haveCompassRequiredSensors(): Boolean {
    val compassSensor = getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    val accelerometerSensor = getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    return compassSensor != null && accelerometerSensor != null
}

fun SensorManager.registerCompassListener(compassListener: CompassListener) {
    registerListener(
        compassListener, getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
        SensorManager.SENSOR_DELAY_NORMAL
    )
    registerListener(
        compassListener, getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
        SensorManager.SENSOR_DELAY_NORMAL
    )
}