package pl.pawel.compass.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.pawel.compass.data.Location
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor() : ViewModel() {

    var shouldStartGettingLocalization: Boolean = false
    private var _rotation = MutableLiveData<Float>()
    val rotation: LiveData<Float> = _rotation

    fun updateRotation(rotation: Float) {
        _rotation.value = rotation
    }

    fun updateLocation(it: Location) {
        TODO("Not yet implemented")
        /*Log.d(
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
        Log.d("CompassFragment", "onLocationResult: angle: $angle")*/
    }
}