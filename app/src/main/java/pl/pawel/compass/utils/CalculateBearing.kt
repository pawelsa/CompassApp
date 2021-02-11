package pl.pawel.compass.utils

import pl.pawel.compass.data.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object CalculateBearing {

    fun getDegree(firstLocation: Location, secondLocation: Location): Float {
        val firstInRadians =
            Location(firstLocation.latitude.toRadians(), firstLocation.longitude.toRadians())
        val secondInRadians =
            Location(secondLocation.latitude.toRadians(), secondLocation.longitude.toRadians())

        val deltaLongitude = secondInRadians.longitude - firstInRadians.longitude
        val y = cos(secondInRadians.latitude) * sin(deltaLongitude)
        val x = cos(firstInRadians.latitude) * sin(secondInRadians.latitude) -
                sin(firstInRadians.latitude) * cos(secondInRadians.latitude) *
                cos(deltaLongitude)
        return atan2(y, x).toDegrees()
    }

    private fun Float.toRadians(): Float {
        return (this * (Math.PI / 180)).toFloat()
    }

    private fun Float.toDegrees(): Float {
        return (this * (180 / Math.PI)).toFloat()
    }

}