package pl.pawel.compass.dialogs

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import pl.pawel.compass.R

object EnableGpsDialog {
    fun showEnableGPSDialog(context: Context, onCancel: () -> Unit) {
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.enable_gps))
                .setMessage(context.getString(R.string.required_for_this_app))
                .setCancelable(true)
                .setPositiveButton(context.getString(R.string.enable_now)) { _, _ ->
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                .setOnCancelListener {
                    onCancel()
                }
                .show()
    }
}