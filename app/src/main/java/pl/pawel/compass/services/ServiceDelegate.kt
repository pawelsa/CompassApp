package pl.pawel.compass.services

import pl.pawel.compass.base.BaseFragmentWithService
import pl.pawel.compass.base.BaseService
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class ServiceDelegate<T : BaseService> : ReadOnlyProperty<BaseFragmentWithService<T>, T?> {
    override fun getValue(thisRef: BaseFragmentWithService<T>, property: KProperty<*>): T? {
        return thisRef.serviceBinder.service
    }
}