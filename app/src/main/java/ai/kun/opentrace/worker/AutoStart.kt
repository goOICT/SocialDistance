package ai.kun.opentrace.worker

import ai.kun.opentrace.util.Constants
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AutoStart : BroadcastReceiver() {
    private val mBleServer : BLEServer = BLEServer()
    private val mBleClient : BLEClient = BLEClient()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            BLETrace.init(context.applicationContext)
            mBleServer.enable(Constants.BACKGROUND_TRACE_INTERVAL)
            mBleClient.enable(Constants.BACKGROUND_TRACE_INTERVAL)
        }
    }
}