package ai.kun.socialdistancealarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ai.kun.opentracesdk_fat.BLETrace

/**
 * Auto start the application so that even if the device is re-booted it will still alert.
 * Note that we did not test this functionality extensively.
 */
class AutoStart : BroadcastReceiver() {
    /**
     * Get notified when reboot has happened and restart tracing
     *
     * @param context The current context to execute in
     * @param intent The system intent.  We act on the reboot broadcast intent only.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            BLETrace.init(context.applicationContext)
            BLETrace.start(true)
        }
    }
}