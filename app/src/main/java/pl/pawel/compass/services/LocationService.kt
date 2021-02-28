package pl.pawel.compass.services

import android.app.*
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.disposables.Disposable
import pl.pawel.compass.data.model.Location
import pl.pawel.compass.data.use_case.ObserveLocationUseCase
import pl.pawel.compass.utils.NotificationUtil.createLocationChannel
import pl.pawel.compass.utils.NotificationUtil.startLocationForeground
import pl.pawel.compass.utils.RxBus
import javax.inject.Inject


@AndroidEntryPoint
class LocationService : Service() {
    @Inject
    lateinit var locationUseCase: ObserveLocationUseCase
    private val binder = LocationBinder()
    private var changingConfiguration = false
    private val notificationManager by lazy {
        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }
    private var currentLocation: Location? = null
    private var locationDisposable: Disposable? = null

    override fun onBind(intent: Intent?): IBinder {
        setupOnBind()
        return binder
    }

    override fun onRebind(intent: Intent?) {
        setupOnBind()
        super.onRebind(intent)
    }

    private fun setupOnBind() {
        stopForeground(true)
        setupObservingLocation()
        changingConfiguration = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        if (!changingConfiguration && locationDisposable?.isDisposed == false) {
            startLocationForeground(currentLocation)
        }
        return true
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager.createLocationChannel(applicationContext)
    }

    private fun setupObservingLocation() {
        Log.d("LocationService", "setupObservingLocation: ")
        locationDisposable = locationUseCase()
            .doOnSubscribe {
                Log.d("LocationService", "setupObservingLocation: onSubscribe")
            }
            .subscribe({
                Log.d("LocationService", "setupObservingLocation: $it")
                currentLocation = it
                RxBus.publish(it)
            }, {
                Log.d("LocationService", "setupObservingLocation: $it")
                setupObservingLocation()
            })
    }

    override fun onDestroy() {
        stopObservingLocation()
        super.onDestroy()
    }

    private fun stopObservingLocation() {
        locationDisposable?.dispose()
    }

    inner class LocationBinder : Binder() {
        val service: LocationService = this@LocationService
    }
}