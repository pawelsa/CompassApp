package pl.pawel.compass.services

import android.app.*
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
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
        Log.d("LocationService", "setupOnBind: ")
        stopForeground(true)
        setupObservingLocation()
        changingConfiguration = false
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.d("LocationService", "onUnbind: ")
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
        Log.d("LocationService", "onCreate: ")
        notificationManager.createLocationChannel(applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("LocationService", "onStartCommand: ${intent?.action ?: ""}")
        when (intent?.action) {
            START_OBTAINING_LOCATION -> setupObservingLocation()
            STOP_OBSERVING_LOCATION -> stopObservingLocation()
        }
        return START_NOT_STICKY
    }

    private fun setupObservingLocation() {
        Log.d("LocationService", "setupObservingLocation: ")
        locationDisposable = locationUseCase()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                currentLocation = it
                RxBus.publish(it)
            }, {
                setupObservingLocation()
            })
    }

    private fun stopObservingLocation() {
        locationDisposable?.dispose()
    }

    inner class LocationBinder : Binder() {
        val service: LocationService = this@LocationService
    }

    companion object {
        val STOP_OBSERVING_LOCATION = LocationService::class.java.name + "stop_observing"
        val START_OBTAINING_LOCATION = LocationService::class.java.name + "start_observing"
    }

}