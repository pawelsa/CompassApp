package pl.pawel.compass.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.utils.toAppLocation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationObserver @Inject constructor(
    context: Context
) {
    private val locationSubject: PublishSubject<Location> = PublishSubject.create()
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val locationRequest = LocationRequest()
        .setInterval(NORMAL_INTERVAL)
        .setFastestInterval(FAST_INTERVAL)
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            locationResult?.locations?.forEach(::updateLocation)
        }
    }

    val locationObservable: Flowable<Location> =
            locationSubject.toFlowable(BackpressureStrategy.LATEST)
                    .observeOn(Schedulers.io())
                    .doOnSubscribe { startObtainingLocalization() }
                    .doOnCancel { stopObtainingLocation() }

    companion object {
        private const val NORMAL_INTERVAL = 5000L
        private const val FAST_INTERVAL = 2000L
    }

    private fun updateLocation(location: android.location.Location?) {
        location?.let {
            locationSubject.onNext(it.toAppLocation())
        }
    }

    @SuppressLint("MissingPermission")
    private fun startObtainingLocalization() {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener(::updateLocation)
        fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
        )
    }

    private fun stopObtainingLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}