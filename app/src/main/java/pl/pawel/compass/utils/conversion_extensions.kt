package pl.pawel.compass.utils

fun Double.distanceToString(): String = when {
    this > 1000000 -> (this / 1000).format(1) + "k km"
    this > 1000 -> (this / 1000).format(1) + " km"
    else -> this.format(2) + " m"
}

private fun Double.format(digits: Int): String = "%.${digits}f".format(this)

fun Float.toRadians(): Float = (this * (Math.PI / 180)).toFloat()

fun Float.toDegrees(): Float = (this * (180 / Math.PI)).toFloat()