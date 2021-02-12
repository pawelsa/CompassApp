package pl.pawel.compass.compass

import android.annotation.SuppressLint
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.location.*
import pl.pawel.compass.data.Location
import pl.pawel.compass.utils.toAppLocation

class LocationListener(
    appCompatActivity: AppCompatActivity,
    private val locationUpdates: (Location) -> Unit
) {
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(appCompatActivity)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            for (location in locationResult.locations) {
                locationUpdates(location.toAppLocation())
            }
        }
    }

    companion object {
        private const val NORMAL_INTERVAL = 5000L
        private const val FAST_INTERVAL = 2000L
    }

    fun startObservingLocation(
        appCompatActivity: AppCompatActivity,
        onSuccess: () -> Unit = {},
        onError: () -> Unit = {}
    ) {

        val locationRequest = LocationRequest()
            .setInterval(NORMAL_INTERVAL)
            .setFastestInterval(FAST_INTERVAL)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        val locationRequestBuilder =
            LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
                .setAlwaysShow(true)

        LocationServices.getSettingsClient(appCompatActivity)
            .checkLocationSettings(locationRequestBuilder.build())
            .addOnSuccessListener {
                startObtainingLocalization(locationRequest)
                onSuccess()
            }.addOnFailureListener {
                onError()
            }
    }


    @SuppressLint("MissingPermission")
    private fun startObtainingLocalization(
        locationRequest: LocationRequest?
    ) {
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    fun stopObtainingLocation() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

}