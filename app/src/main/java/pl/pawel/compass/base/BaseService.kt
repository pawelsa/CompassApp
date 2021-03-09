package pl.pawel.compass.base

import android.app.Service
import android.os.Binder

abstract class BaseService : Service() {
    abstract inner class BaseBinder : Binder() {
        abstract val service: BaseService
    }
}