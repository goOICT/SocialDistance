package ai.kun.socialdistancealarm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ai.kun.opentracesdk_fat.BLETrace

class AutoStart : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            BLETrace.init(context.applicationContext)
            BLETrace.start(true)
        }
    }
}