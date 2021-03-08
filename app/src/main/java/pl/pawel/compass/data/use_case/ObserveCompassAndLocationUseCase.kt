package pl.pawel.compass.data.use_case

import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.data.repository.SensorRepository
import pl.pawel.compass.utils.GeographicCalculations
import pl.pawel.compass.utils.RxBus
import javax.inject.Inject

interface ObserveCompassAndLocationUseCase {
    operator fun invoke(param: Param): Flowable<CompassAndLocationState>
    data class Param(val observeType: ObserveType)

    sealed class ObserveType {
        object Compass : ObserveType()
        data class Both(val destination: Location) : ObserveType()
    }

    sealed class CompassAndLocationState(val bearing: Float) {
        class OnlyCompass(bearing: Float) : CompassAndLocationState(bearing)
        class CompassWithLocalization(
                bearing: Float,
                val bearingOfDestination: Float,
                val distanceToDestination: Double
        ) : CompassAndLocationState(bearing)
    }

}

class ObserveCompassAndLocationUseCaseImpl @Inject constructor(private val sensorRepository: SensorRepository) : ObserveCompassAndLocationUseCase {
    private var bearing: Float = 0f
    override fun invoke(param: ObserveCompassAndLocationUseCase.Param): @NonNull Flowable<ObserveCompassAndLocationUseCase.CompassAndLocationState> {
        return when (param.observeType) {
            is ObserveCompassAndLocationUseCase.ObserveType.Both -> handleBoth(param.observeType)
            is ObserveCompassAndLocationUseCase.ObserveType.Compass -> handleOnlyCompass()
        }
    }

    // TODO: 3/8/21 better handle situation when location is starting, because app looks frozen when waiting for location update
    private fun handleBoth(observeType: ObserveCompassAndLocationUseCase.ObserveType.Both): Flowable<ObserveCompassAndLocationUseCase.CompassAndLocationState> =
            Flowable.combineLatest(sensorRepository.observeCompass(), RxBus.listen(Location::class.java).toFlowable(BackpressureStrategy.LATEST),
                    { rotation: Float, location: Location ->
                        bearing = -rotation
                        val angle = GeographicCalculations.getDegree(location, observeType.destination)
                        val destinationAngle = angle + bearing
                        val distance = GeographicCalculations.getDistance(location, observeType.destination)
                        ObserveCompassAndLocationUseCase.CompassAndLocationState.CompassWithLocalization(bearing, destinationAngle, distance)
                    })

    private fun handleOnlyCompass(): @NonNull Flowable<ObserveCompassAndLocationUseCase.CompassAndLocationState> =
            sensorRepository.observeCompass().map { rotation ->
                bearing = -rotation
                ObserveCompassAndLocationUseCase.CompassAndLocationState.OnlyCompass(bearing)
            }
}