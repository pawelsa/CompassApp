package pl.pawel.compass.screen.compass

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.utils.GeographicCalculations
import pl.pawel.compass.utils.RxBus
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor() : ViewModel() {
    var shouldStartGettingLocalization: Boolean = false

    private val _state = MutableLiveData<CompassState>()
    val state: LiveData<CompassState> = _state

    private var bearing: Float = 0f
    private var myLocation: Location? = null
    private var destination: Location? = null
    private val disposables = CompositeDisposable()

    fun updateRotation(rotation: Float) {
        bearing = -rotation
        updateData()
    }

    fun updateDestination(destination: Location) {
        this.destination = destination
        updateData()
    }

    private fun updateData() {
        if (destination != null && myLocation != null) {
            val angle = GeographicCalculations.getDegree(myLocation!!, destination!!)
            val destinationAngle = angle + bearing
            val distance = GeographicCalculations.getDistance(myLocation!!, destination!!)
            _state.value =
                CompassState.CompassWithLocalizationState(bearing, destinationAngle, distance)
        } else {
            _state.value = CompassState.OnlyCompass(bearing)
        }
    }

    fun startObservingLocation() {
        disposables += RxBus.listen(Location::class.java)
            .subscribe(::updateMyLocation) {
                // TODO: 01.03.2021 handle throwable
            }
    }

    private fun updateMyLocation(location: Location) {
        Log.d("CompassViewModel", "updateMyLocation: $location")
        myLocation = location
        updateData()
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