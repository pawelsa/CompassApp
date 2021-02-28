package pl.pawel.compass.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.pawel.compass.data.repository.LocationRepository
import pl.pawel.compass.data.repository.LocationRepositoryImpl
import pl.pawel.compass.data.use_case.ObserveLocationUseCase
import pl.pawel.compass.data.use_case.ObserveLocationUseCaseImpl
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
    fun provideLocationRepository(locationObserver: LocationObserver): LocationRepository =
        LocationRepositoryImpl(locationObserver)

    @Singleton
    @Provides
    fun provideLocationUseCase(locationRepository: LocationRepository): ObserveLocationUseCase =
        ObserveLocationUseCaseImpl(locationRepository)
}

/*@Module
@InstallIn(ServiceComponent::class, SingletonComponent::class) // Installs FooModule in the generate SingletonComponent.
object ServiceModules {

    @Provides
    fun provideLocationObserver(@ApplicationContext context: Context): LocationObserver = LocationObserver(context)

    @Provides
    fun provideLocationRepository(locationObserver: LocationObserver): LocationRepository = LocationRepositoryImpl(locationObserver)

    @Provides
    fun provideLocationUseCase(locationRepository: LocationRepository): ObserveLocationUseCase = ObserveLocationUseCaseImpl(locationRepository)
}*/
