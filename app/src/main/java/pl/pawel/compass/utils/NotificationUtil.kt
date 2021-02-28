package pl.pawel.compass.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import pl.pawel.compass.R
import pl.pawel.compass.activities.MainActivity
import pl.pawel.compass.data.model.Location

object NotificationUtil {
    private const val CHANNEL_ID = "location_channel"
    private const val OPEN_ACTIVITY_PENDING_INTENT_REQUEST_CODE = 0
    private const val LOCATION_NOTIFICATION_ID = 153432352

    fun getNotification(context: Context, location: Location?): Notification? {
        val text: CharSequence = if (location != null) context.getString(
            R.string.current_location,
            location.longitude,
            location.longitude
        ) else context.getString(R.string.establishing_location)

        val activityPendingIntent = PendingIntent.getActivity(
            context, OPEN_ACTIVITY_PENDING_INTENT_REQUEST_CODE,
            Intent(context, MainActivity::class.java), 0
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .addAction(
                android.R.drawable.sym_def_app_icon, context.getString(R.string.launch_activity),
                activityPendingIntent
            )
            .setContentText(text)
            .setContentTitle(context.getString(R.string.your_location))
            .setOngoing(true)
            .setPriority(NotificationManager.IMPORTANCE_HIGH)
            .setSmallIcon(R.mipmap.ic_launcher)

        return builder.build()
    }

    fun NotificationManager.createLocationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = context.getString(R.string.app_name)
            val mChannel =
                NotificationChannel(CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT)

            createNotificationChannel(mChannel)
        }
    }

    fun Service.startLocationForeground(location: Location?) {
        this.startForeground(
            LOCATION_NOTIFICATION_ID,
            getNotification(this.applicationContext, location)
        )
    }

}