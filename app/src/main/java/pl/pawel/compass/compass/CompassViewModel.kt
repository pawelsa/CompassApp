package pl.pawel.compass.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor() : ViewModel() {

    private var _rotation = MutableLiveData<Float>()
    val rotation: LiveData<Float> = _rotation

    fun updateRotation(rotation: Float) {
        _rotation.value = rotation
    }
}