package ai.kun.opentrace.worker

import ai.kun.opentrace.util.Constants
import android.app.IntentService
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class BLETrace {
    private val mBleServer : BLEServer = BLEServer()
    private val mBleClient : BLEClient = BLEClient()

    fun startBackground(context: Context) {
        mBleServer.enable(context, Constants.BACKGROUND_TRACE_INTERVAL)
        mBleClient.enable(context, Constants.BACKGROUND_TRACE_INTERVAL)
    }

    fun stopBackground(context: Context) {
        mBleServer.disable(context, Constants.BACKGROUND_TRACE_INTERVAL)
        mBleClient.disable(context, Constants.BACKGROUND_TRACE_INTERVAL)
    }

    fun startForeground(context: Context) {
        mBleServer.enable(context, Constants.FOREGROUND_TRACE_INTERVAL)
        mBleClient.enable(context, Constants.FOREGROUND_TRACE_INTERVAL)
    }

    fun stopForeground(context: Context) {
        mBleServer.disable(context, Constants.FOREGROUND_TRACE_INTERVAL)
        mBleClient.disable(context, Constants.FOREGROUND_TRACE_INTERVAL)
    }

}