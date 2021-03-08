package pl.pawel.compass.screen.compass

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.data.use_case.ObserveCompassAndLocationUseCase
import javax.inject.Inject

@HiltViewModel
class CompassViewModel @Inject constructor(private val observeCompassAndLocationUseCase: ObserveCompassAndLocationUseCase) : ViewModel() {
    var shouldStartGettingLocalization: Boolean = false

    // TODO: 3/8/21 check if can be done with observable delegate
    private var destination: Location? = null

    private val _state = MutableLiveData<ScreenState>()
    val state: LiveData<ScreenState> = _state

    private var compassAndLocationDisposable: Disposable? = null

    fun startObserving(destination: Location? = null) {
        destination?.let { this.destination = it }
        val param = when (this.destination) {
            null -> ObserveCompassAndLocationUseCase.ObserveType.Compass
            else -> ObserveCompassAndLocationUseCase.ObserveType.Both(this.destination!!)
        }
        if (compassAndLocationDisposable?.isDisposed == false) {
            compassAndLocationDisposable?.dispose()
        }
        compassAndLocationDisposable = observeCompassAndLocationUseCase(ObserveCompassAndLocationUseCase.Param(param))
                .map { it.toScreenState() }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    _state.value = it
                }, {
                    // TODO: 3/8/21 handle throwable
                })
    }

    fun stopObserving() {
        compassAndLocationDisposable?.dispose()
    }

    override fun onCleared() {
        compassAndLocationDisposable?.dispose()
        super.onCleared()
    }


    sealed class ScreenState(val bearing: Float) {
        class OnlyCompass(bearing: Float) : ScreenState(bearing)
        class CompassWithLocalization(
                bearing: Float,
                val bearingOfDestination: Float,
                val distanceToDestination: Double
        ) : ScreenState(bearing)
    }

    private fun ObserveCompassAndLocationUseCase.CompassAndLocationState.toScreenState() = when (this) {
        is ObserveCompassAndLocationUseCase.CompassAndLocationState.CompassWithLocalization ->
            ScreenState.CompassWithLocalization(bearing, bearingOfDestination, distanceToDestination)
        else -> ScreenState.OnlyCompass(bearing)
    }
}
