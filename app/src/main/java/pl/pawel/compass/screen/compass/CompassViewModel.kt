package pl.pawel.compass.screen.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.schedulers.Schedulers
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.data.use_case.CompassUseCase
import pl.pawel.compass.utils.GeographicCalculations
import pl.pawel.compass.utils.RxBus
import javax.inject.Inject

// TODO: 01.03.2021 move more of the code to usecases etc

@HiltViewModel
class CompassViewModel @Inject constructor(private val compassUseCase: CompassUseCase) : ViewModel() {
    var shouldStartGettingLocalization: Boolean = false

    private val _state = MutableLiveData<CompassState>()
    val state: LiveData<CompassState> = _state

    private var bearing: Float = 0f
    private var myLocation: Location? = null
    private var destination: Location? = null
    private val disposables = CompositeDisposable()

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

    fun startObservingCompass() {
        disposables += compassUseCase()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ rotation ->
                    bearing = -rotation
                    updateData()
                }, {
                    // TODO: 3/8/21 handle throwable
                })
    }

    fun stopObserving() {
        disposables.dispose()
    }

    private fun updateMyLocation(location: Location) {
        myLocation = location
        updateData()
    }

    override fun onCleared() {
        disposables.clear()
        super.onCleared()
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

enum class ObserveType { COMPASS, BOTH }