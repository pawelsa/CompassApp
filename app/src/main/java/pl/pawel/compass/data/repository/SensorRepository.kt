package pl.pawel.compass.data.repository

import io.reactivex.rxjava3.core.Flowable
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.sensors.CompassObserver
import pl.pawel.compass.sensors.LocationObserver
import javax.inject.Inject

interface SensorRepository {
    fun obtainLocation(): Flowable<Location>
    fun observeCompass(): Flowable<Float>
}

class SensorRepositoryImpl @Inject constructor(private val locationObserver: LocationObserver, private val compassObserver: CompassObserver) :
        SensorRepository {
    override fun obtainLocation(): Flowable<Location> = locationObserver.locationObservable
    override fun observeCompass(): Flowable<Float> = compassObserver.observer
}