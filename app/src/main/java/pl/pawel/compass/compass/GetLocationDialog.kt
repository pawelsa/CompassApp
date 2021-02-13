package pl.pawel.compass.compass

import android.content.DialogInterface
import android.view.LayoutInflater
import androidx.core.widget.doOnTextChanged
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import pl.pawel.compass.R
import pl.pawel.compass.data.Location
import pl.pawel.compass.databinding.DialogEditTextFieldBinding
import pl.pawel.compass.utils.isNotValid
import pl.pawel.compass.utils.toFloat
import kotlin.math.absoluteValue

object GetLocationDialog {

    fun showDialogToSelectDestination(
        layoutInflater: LayoutInflater,
        result: (Location) -> Unit
    ) {
        val context = layoutInflater.context
        val dialogLayout = DialogEditTextFieldBinding.inflate(layoutInflater)
        val latitudeEditText = dialogLayout.etLatitude
        val longitudeEditText = dialogLayout.etLongitude

        latitudeEditText.editText?.doOnTextChanged { _, _, _, _ ->
            latitudeEditText.checkCoordinatesValidity(90f)
        }

        longitudeEditText.editText?.doOnTextChanged { _, _, _, _ ->
            longitudeEditText.checkCoordinatesValidity(180f)
        }

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.resources.getString(R.string.enter_location))
            .setPositiveButton(android.R.string.ok) { _, _ -> }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .setView(dialogLayout.root)
            .setCancelable(true)
            .create()


        dialog.show()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
            if (longitudeEditText.isNotValid() || latitudeEditText.isNotValid()) {
                latitudeEditText.checkCoordinatesValidity(90f)
                longitudeEditText.checkCoordinatesValidity(180f)
                return@setOnClickListener
            }
            val latitude = latitudeEditText.editText?.text?.toFloat() ?: 0f
            val longitude = longitudeEditText.editText?.text?.toFloat() ?: 0f

            val destination = Location(
                latitude = latitude,
                longitude = longitude
            )
            result(destination)
            dialog.dismiss()
        }
    }

    private fun TextInputLayout.checkCoordinatesValidity(
        borderValue: Float
    ) {
        val text = this.editText?.text
        val longitude = text?.toString()?.toFloatOrNull()
        error = when {
            text?.isEmpty() == true -> context.getString(R.string.error_empty_field)
            longitude == null || longitude.absoluteValue > borderValue -> {
                context.getString(R.string.error_value_between, borderValue)
            }
            else -> null
        }
    }
}