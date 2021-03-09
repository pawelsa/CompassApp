package pl.pawel.compass.base

import pl.pawel.compass.services.ServiceDelegate

abstract class BaseFragmentWithService<SERVICE : BaseService> : BaseFragment() {
    val service: SERVICE? by ServiceDelegate()
    val serviceBinder: ServiceBinder<SERVICE> = ServiceBinder()
}