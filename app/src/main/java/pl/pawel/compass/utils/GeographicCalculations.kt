package pl.pawel.compass.utils

import pl.pawel.compass.data.model.Location
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeographicCalculations {

    /**
     * Calculate angle between two localizations with respect to north
     * */
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

    /**
     * Get distance in meters between two coordinates using Haversine formula
     * */
    fun getDistance(firstLocation: Location, secondLocation: Location): Double {
        val earthRadius = 6371e3
        val lat1 = firstLocation.latitude.toRadians()
        val lat2 = secondLocation.latitude.toRadians()
        val deltaLat = lat2 - lat1
        val deltaLon = (secondLocation.longitude - firstLocation.longitude).toRadians()

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return earthRadius * c
    }

}