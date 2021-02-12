package pl.pawel.compass.utils

import android.location.Location

fun Location.toAppLocation(): pl.pawel.compass.data.Location {
    return pl.pawel.compass.data.Location(this.latitude.toFloat(), this.longitude.toFloat())
}