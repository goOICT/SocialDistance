package ai.kun.opentracesdk_fat.util

import ai.kun.opentracesdk_fat.R
import ai.kun.opentracesdk_fat.BLETrace
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer

/**
 * A set of utilities to help with notifications
 */
object NotificationUtils {
    // Constants for the notification actions buttons.
    public const val ACTION_TOO_CLOSE_NOTIFICATION =
        "ai.kun.socialdistancealarm.ACTION_TOO_CLOSE_NOTIFICATION"

    public const val ACTION_DANGER_NOTIFICATION =
        "ai.kun.socialdistancealarm.ACTION_DANGER_NOTIFICATION"

    // Notification channel ID.
    private const val TOO_CLOSE_CHANNEL_ID = "too_close_notification_channel"
    private const val DANGER_CHANNEL_ID = "danger_notification_channel"

    // Notification ID.
    private const val NOTIFICATION_ID = 0

    private lateinit var context: Context
    private lateinit var notifyManager: NotificationManager


    /**
     * Initialize the notifications.
     *
     * @param context The Context you'll be sending notifications from
     */
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

            // Create the NotificationChannels with all the parameters.
            val notificationChannelTooClose = NotificationChannel(
                TOO_CLOSE_CHANNEL_ID,
                context.getString(R.string.notification_channel_name_too_close),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannelTooClose.enableLights(true)
            notificationChannelTooClose.lightColor = Color.RED
            notificationChannelTooClose.enableVibration(true)
            notificationChannelTooClose.description = context.getString(R.string.notification_channel_description_too_close)
            notifyManager.createNotificationChannel(notificationChannelTooClose)

            val notificationChannelDanger = NotificationChannel(
                DANGER_CHANNEL_ID,
                context.getString(R.string.notification_channel_name_danger),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannelDanger.enableLights(true)
            notificationChannelDanger.lightColor = Color.YELLOW
            notificationChannelDanger.enableVibration(true)
            notificationChannelDanger.description = context.getString(R.string.notification_channel_description_danger)
            notifyManager.createNotificationChannel(notificationChannelDanger)
        }

        // Listen for pause and clear notifications...
        BLETrace.isStarted.observeForever(Observer { isStarted ->
            if (!isStarted) {
                notifyManager.cancelAll()
            }
        })
    }

    /**
     * Send the too close notification
     *
     */
    fun sendNotificationTooClose() {
        BLETrace.isStarted.value?.let {
            if (it) {
                // Sets up the pending intent to update the notification.
                val updateIntent = Intent(ACTION_TOO_CLOSE_NOTIFICATION)
                val updatePendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT
                )

                // Build the notification with all of the parameters using helper
                // method.
                val notifyBuilder = getNotificationBuilderTooClose()

                // Deliver the notification.
                notifyManager.notify(NOTIFICATION_ID, notifyBuilder.build())
            }
        }
    }

    /**
     * send the danger notification
     *
     */
    fun sendNotificationDanger() {
        BLETrace.isStarted.value?.let {
            if (it) {
                // Sets up the pending intent to update the notification.
                val updateIntent = Intent(ACTION_DANGER_NOTIFICATION)
                val updatePendingIntent = PendingIntent.getBroadcast(
                    context,
                    NOTIFICATION_ID, updateIntent, PendingIntent.FLAG_ONE_SHOT
                )

                // Build the notification with all of the parameters using helper
                // method.
                val notifyBuilder = getNotificationBuilderDanger()

                // Deliver the notification.
                notifyManager.notify(NOTIFICATION_ID, notifyBuilder.build())
            }
        }
    }

    /**
     * Helper method that builds the notification.
     *
     * @return NotificationCompat.Builder: notification build with all the
     * parameters.
     */
    private fun getNotificationBuilderTooClose(): NotificationCompat.Builder {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        //TODO: replace hard coded class string with an extra passed to alarm manager
        val notificationIntent = Intent(context, Class.forName("ai.kun.socialdistancealarm.MainActivity"))
        val notificationPendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification with all of the parameters.
        return NotificationCompat.Builder(context, TOO_CLOSE_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.too_close_notification_title))
            .setContentText(context.getString(R.string.too_close_notification_text))
            .setSmallIcon(R.drawable.ic_report_red_24dp)
            .setAutoCancel(true).setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)!!
    }
    /**
     * Helper method that builds the notification.
     *
     * @return NotificationCompat.Builder: notification build with all the
     * parameters.
     */
    private fun getNotificationBuilderDanger(): NotificationCompat.Builder {

        // Set up the pending intent that is delivered when the notification
        // is clicked.
        //TODO: replace hard coded class string with an extra passed to alarm manager
        val notificationIntent = Intent(context, Class.forName("ai.kun.socialdistancealarm.MainActivity"))
        val notificationPendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build the notification with all of the parameters.
        return NotificationCompat.Builder(context, DANGER_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.danger_notification_title))
            .setContentText(context.getString(R.string.danger_notification_text))
            .setSmallIcon(R.drawable.ic_warning_orange_24dp)
            .setAutoCancel(true).setContentIntent(notificationPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(NotificationCompat.DEFAULT_ALL)!!
    }

}