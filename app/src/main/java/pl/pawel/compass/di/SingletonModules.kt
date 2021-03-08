package pl.pawel.compass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.pawel.compass.data.repository.SensorRepository
import pl.pawel.compass.data.repository.SensorRepositoryImpl
import pl.pawel.compass.data.use_case.CompassUseCase
import pl.pawel.compass.data.use_case.CompassUseCaseImpl
import pl.pawel.compass.data.use_case.ObserveLocationUseCase
import pl.pawel.compass.data.use_case.ObserveLocationUseCaseImpl
import pl.pawel.compass.screen.compass.CompassObserver
import pl.pawel.compass.screen.compass.LocationObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Installs FooModule in the generate SingletonComponent.
object SingletonModules {

    @Singleton
    @Provides
    fun provideLocationObserver(@ApplicationContext context: Context): LocationObserver =
            LocationObserver(context)

    @Singleton
    @Provides
    fun provideLocationRepository(locationObserver: LocationObserver, compassObserver: CompassObserver): SensorRepository =
            SensorRepositoryImpl(locationObserver, compassObserver)

    @Singleton
    @Provides
    fun provideLocationUseCase(sensorRepository: SensorRepository): ObserveLocationUseCase =
            ObserveLocationUseCaseImpl(sensorRepository)

    @Singleton
    @Provides
    fun provideCompassObserver(@ApplicationContext context: Context): CompassObserver = CompassObserver(context)

    @Singleton
    @Provides
    fun provideCompassUseCase(sensorRepository: SensorRepository): CompassUseCase = CompassUseCaseImpl(sensorRepository)

}
