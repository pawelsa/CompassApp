package pl.pawel.compass.screen.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.pawel.compass.data.Location
import pl.pawel.compass.utils.GeographicCalculations
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor() : ViewModel() {
    var shouldStartGettingLocalization: Boolean = false

    private val _state = MutableLiveData<CompassState>()
    val state: LiveData<CompassState> = _state

    private var bearing: Float = 0f
    private var myLocation: Location? = null
    private var destination: Location? = null

    fun updateRotation(rotation: Float) {
        bearing = -rotation
        updateData()
    }

    fun updateMyLocation(location: Location) {
        myLocation = location
        updateData()
    }

    fun updateDestination(destination: Location) {
        this.destination = destination
        updateData()
    }

    private fun updateData() {
        if (destination == null) {
            _state.value = CompassState.OnlyCompass(bearing)
        } else if (destination != null && myLocation != null) {
            val angle = GeographicCalculations.getDegree(myLocation!!, destination!!)
            val destinationAngle = angle + bearing
            val distance = GeographicCalculations.getDistance(myLocation!!, destination!!)
            _state.value =
                CompassState.CompassWithLocalizationState(bearing, destinationAngle, distance)
        }
    }
}

sealed class CompassState(val bearing: Float) {
    class OnlyCompass(bearing: Float) : CompassState(bearing)
    class CompassWithLocalizationState(
        bearing: Float,
        val bearingOfDestination: Float,
        val distanceToDestination: Double
    ) : CompassState(bearing)
}