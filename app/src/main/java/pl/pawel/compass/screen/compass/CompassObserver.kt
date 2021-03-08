package pl.pawel.compass.screen.compass

import android.content.Context
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject
import pl.pawel.compass.utils.haveCompassRequiredSensors
import pl.pawel.compass.utils.registerCompassListener
import javax.inject.Inject

class CompassObserver @Inject constructor(context: Context) {
    private val sensorManager: SensorManager by lazy {
        ContextCompat.getSystemService(context, SensorManager::class.java) as SensorManager
    }
    private val compassListener by lazy {
        CompassListener(_observer::onNext)
    }
    private val _observer = PublishSubject.create<Float>()
    val observer: Flowable<Float> =
            _observer.toFlowable(BackpressureStrategy.LATEST)
                    .observeOn(Schedulers.io())
                    .doOnSubscribe { startObservingCompass() }
                    .doOnCancel { stopObservingCompass() }

    private fun startObservingCompass() {
        if (sensorManager.haveCompassRequiredSensors()) {
            sensorManager.registerCompassListener(compassListener)
        } else {
            // TODO: 3/8/21 handle scenario when device do not have the sensors
        }
    }

    private fun stopObservingCompass() {
        sensorManager.unregisterListener(compassListener)
    }

}