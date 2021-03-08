package pl.pawel.compass.data.use_case

import io.reactivex.rxjava3.core.Flowable
import pl.pawel.compass.data.repository.SensorRepository
import javax.inject.Inject

interface CompassUseCase {
    operator fun invoke(): Flowable<Float>
}

class CompassUseCaseImpl @Inject constructor(private val sensorRepository: SensorRepository) : CompassUseCase {
    override fun invoke(): Flowable<Float> = sensorRepository.observeCompass()
}
