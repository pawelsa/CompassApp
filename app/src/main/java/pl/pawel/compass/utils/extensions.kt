package pl.pawel.compass.utils

import android.location.Location
import android.text.Editable
import com.google.android.material.textfield.TextInputLayout

fun Location.toAppLocation(): pl.pawel.compass.data.Location {
    return pl.pawel.compass.data.Location(this.latitude.toFloat(), this.longitude.toFloat())
}

fun Editable?.toFloat(): Float = this?.toString()?.toFloat() ?: 0f

fun TextInputLayout.isNotValid(): Boolean = editText?.text?.isEmpty() != false || error != null