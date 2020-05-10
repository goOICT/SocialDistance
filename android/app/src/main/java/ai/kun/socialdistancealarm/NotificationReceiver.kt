package ai.kun.socialdistancealarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent


/**
 * The broadcast receiver class for notifications.
 * Responds to the update notification pending intent action.
 */
class NotificationReceiver : BroadcastReceiver() {
    /**
     * Receives the incoming broadcasts and responds accordingly.
     *
     * @param context Context of the app when the broadcast is received.
     * @param intent The broadcast intent containing the action.
     */
    override fun onReceive(context: Context?, intent: Intent?) {
        // Update the notification.
        //TODO: launch home?
    }
}