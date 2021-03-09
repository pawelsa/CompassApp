package pl.pawel.compass.base

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class ServiceBinder<T : BaseService> : ServiceConnection {
    var service: T? = null
    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        val binder: BaseService.BaseBinder =
                iBinder as BaseService.BaseBinder
        service = binder.service as T
    }

    override fun onServiceDisconnected(name: ComponentName) {
        service = null
    }
}