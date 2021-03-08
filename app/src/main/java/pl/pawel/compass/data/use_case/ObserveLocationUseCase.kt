package pl.pawel.compass.data.use_case

import io.reactivex.rxjava3.core.Flowable
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.data.repository.SensorRepository
import javax.inject.Inject

interface ObserveLocationUseCase {
    operator fun invoke(): Flowable<Location>
}

class ObserveLocationUseCaseImpl @Inject constructor(private val sensorRepository: SensorRepository) :
        ObserveLocationUseCase {
    override fun invoke(): Flowable<Location> = sensorRepository.obtainLocation()
}