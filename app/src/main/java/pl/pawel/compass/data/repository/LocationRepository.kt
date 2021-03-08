package pl.pawel.compass.data.repository

import io.reactivex.rxjava3.core.Flowable
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.screen.compass.LocationObserver
import javax.inject.Inject

interface LocationRepository {
    fun obtainLocation(): Flowable<Location>
}

class LocationRepositoryImpl @Inject constructor(private val locationObserver: LocationObserver) :
        LocationRepository {
    override fun obtainLocation(): Flowable<Location> = locationObserver.locationObservable
}