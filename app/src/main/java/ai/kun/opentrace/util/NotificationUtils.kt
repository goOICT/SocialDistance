package ai.kun.opentrace.util

import ai.kun.opentrace.MainActivity
import ai.kun.opentrace.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat


object NotificationUtils {
    // Constants for the notification actions buttons.
    public const val ACTION_TOO_CLOSE_NOTIFICATION =
        "ai.kun.opentrace.ACTION_TOO_CLOSE_NOTIFICATION"

    // Notification channel ID.
    private const val PRIMARY_CHANNEL_ID = "primary_notification_channel"

    // Notification ID.
    private const val NOTIFICATION_ID = 0

    private lateinit var context: Context
    private lateinit var notifyManager: NotificationManager


    fun init(context: Context) {
        this.context = context

        // Create a notification manager object.
        // Create a notification manager object.
        notifyManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.

        // Notification channels are only available in OREO and higher.
        // So, add a check on SDK version.
        if (Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            // Create the NotificationChannel with all the parameters.
            val notificationChannel = NotificationChannel(
                PRIMARY_CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = context.getString(R.string.notification_channel_description)
            notifyManager.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * OnClick method for the "Notify Me!" button.
     * Creates and delivers a simple notification.
     */
    public fun sendNotification() {

        // Sets up the pending intent to update the notification.
        val updateIntent = Intent(ACTION_TOO_CLOSE_NOTIFICATION)
        val updatePendingIntent = PendingIntent.getBroadcast(
            context,
            NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT
        )

        // Build the notification with all of the parameters using helper
        // method.
        val notifyBuilder = getNotificationBuilder()

        // Deliver the notification.
        notifyManager.notify(NOTIFICATION_ID, notifyBuilder.build())
    }

    /**
     * Helper method that builds the notification.
     *
     * @return NotificationCompat.Builder: notification build with all the
     * parameters.
     */
    private fun getNotificationBuilder(): NotificationCompat.Builder {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification with all of the parameters.
        return NotificationCompat.Builder(context, PRIMARY_CHANNEL_ID)
            .setContentTitle("Too Close")
            .setContentText("Another App Was Too Close")
            .setSmallIcon(R.drawable.ic_report_red_24dp)
            .setAutoCancel(true).setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)!!
    }

}