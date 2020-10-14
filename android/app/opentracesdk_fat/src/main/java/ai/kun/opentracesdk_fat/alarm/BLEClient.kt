package ai.kun.opentracesdk_fat.alarm

import ai.kun.opentracesdk_fat.BLETrace
import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import ai.kun.opentracesdk_fat.util.Constants
import ai.kun.opentracesdk_fat.util.Constants.ANDROID_MANUFACTURE_ID
import ai.kun.opentracesdk_fat.util.Constants.ANDROID_MANUFACTURE_SUBSTRING
import ai.kun.opentracesdk_fat.util.Constants.ANDROID_MANUFACTURE_SUBSTRING_MASK
import ai.kun.opentracesdk_fat.util.Constants.APPLE_DEVICE_NAME
import ai.kun.opentracesdk_fat.util.Constants.SCAN_PERIOD
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

/**
 * This code implements detection of other devices.  When another device is detected we store the
 * detection in a database.  That lets this library run independently of the rest of the application.
 * In order to support applications written in React and other cross compatible platforms it's possible
 * to provide a callback and have the detection dealt with some other way (e.g. writing to some
 * cross platform database)
 *
 */
class BLEClient : BroadcastReceiver() {
    private val TAG = "BLEClient"
    private val WAKELOCK_TAG = "ai:kun:socialdistancealarm:worker:BLEClient"

    private val INTERVAL_KEY = "interval"
    private val ISREACTNATIVE_KEY = "isReactNative"

    private val RSSI_KEY = "rssi"
    private val UUID_KEY = "uuid"

    private val CLIENT_REQUEST_CODE = 11
    private val START_DELAY = 10

    private var mScanning = false
    private var mConnected = false

    /**
     * We use alarm manager and cheat by scheduling a new alarm every time the alarm goes off.
     * I know the code seems overly complex, but sadly different versions of Android behaved slightly
     * differently and without all of the below code there were crashes.
     *
     * @param context The context we are using to deal with the alarm
     * @param intent The intent used to launch
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "onReceive")
        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        val isReactNative = intent.getBooleanExtra(ISREACTNATIVE_KEY, false)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(BLETrace) {
            // Chain the next alarm...
            BLETrace.init(context.applicationContext, isReactNative)
            next(interval, context.applicationContext)
            if (BLETrace.isEnabled()) startScan(context.applicationContext)
        }
        wl.release()
    }

    /**
     * set up the next alarm manager
     *
     * @param interval The interval in MS
     * @param context The context to use
     */
    fun next(interval: Int, context: Context) {
        val alarmManager = BLETrace.getAlarmManager(context)
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager,
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            getPendingIntent(interval, context))

    }

    /**
     * Enable scanning
     *
     * @param interval The interval at which to restart scanning
     * @param context The context to use
     */
    fun enable(interval: Int, context: Context) {
        val alarmManager = BLETrace.getAlarmManager(context)
        AlarmManagerCompat.setExactAndAllowWhileIdle(alarmManager,
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + START_DELAY,
            getPendingIntent(interval, context)
        )
    }

    /**
     * Disable scanning
     *
     * @param interval The interval at which scanning was started.
     *                 This is needed so that the alarm can be found.
     * @param context The context to use.
     */
    fun disable(interval: Int, context: Context) {
        synchronized(BLETrace) {
            BLETrace.getAlarmManager(context).cancel(getPendingIntent(interval, context))
            stopScan(context)
        }
    }

    /**
     * create the pending intent that will be fired
     *
     * @param interval the interval for the event
     * @param context the context to use
     * @return the intent
     */
    private fun getPendingIntent(interval: Int, context: Context): PendingIntent {
        val intent = Intent(context, BLEClient::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        intent.putExtra(ISREACTNATIVE_KEY, BLETrace.isReactNative)
        return PendingIntent.getBroadcast(
            context,
            CLIENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    /**
     * Start scanning
     *
     * @param context the context to use
     */
    private fun startScan(context: Context) {
        if (mScanning) {
            Log.w(TAG,"Already scanning")
            return
        }

        val androidScanFilter = ScanFilter.Builder()
            .setManufacturerData(ANDROID_MANUFACTURE_ID,
                            ANDROID_MANUFACTURE_SUBSTRING.toByteArray(StandardCharsets.UTF_8),
                            ANDROID_MANUFACTURE_SUBSTRING_MASK.toByteArray(StandardCharsets.UTF_8))
            .build()
        val appleScanFilter = ScanFilter.Builder()
            .setDeviceName(APPLE_DEVICE_NAME)
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            BLETrace.bluetoothLeScanner!!.startScan(listOf(androidScanFilter, appleScanFilter), settings, BtleScanCallback)
            BtleScanCallback.handler.postDelayed(Runnable { stopScan(context) }, SCAN_PERIOD)
            mScanning = true
            Log.d(TAG, "+++++++Started scanning.")
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while starting scanning caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
        }
    }

    /**
     * stop scanning
     *
     * @param context the context to use
     */
    private fun stopScan(context: Context) {

        synchronized(BLETrace) {
            try {
                if (mScanning && BLETrace.bluetoothManager!!.adapter.isEnabled) {
                    BLETrace.bluetoothLeScanner!!.stopScan(BtleScanCallback)
                    scanComplete(context)
                }

            } catch (exception: Exception) {
                val msg = " ${exception::class.qualifiedName} while stopping scanning caused by ${exception.localizedMessage}"
                Log.e(TAG, msg)
            }
            mScanning = false
        }
        Log.d(TAG, "-------Stopped scanning.")
    }

    private fun scanComplete(context: Context) {
        if (BLETrace.isReactNative ) {
            val devices = BtleScanCallback.mScanResults.values
            Intent().also {  intent ->
                intent.action = Constants.INTENT_DEVICE_SCANNED
                intent.putExtra(RSSI_KEY, devices.map { it.rssi }.toIntArray())
                val ids = devices.map { it.deviceUuid }
                intent.putExtra(UUID_KEY, ids.toTypedArray())
                Log.i(TAG, "Sending ids ${ids.size}")
                context.sendBroadcast(intent)
            }
            BtleScanCallback.mScanResults.clear()
            return
        }

        var noCurrentDevices = true

        if (!BtleScanCallback.mScanResults.isEmpty()) {
            for (deviceAddress in BtleScanCallback.mScanResults.keys) {
                val result: Device? = BtleScanCallback.mScanResults.get(deviceAddress)
                result?.let { device ->
                    Log.d(
                        TAG,
                        "+++++++++++++ Traced: device=${device.deviceUuid} rssi=${device.rssi} txPower=${device.txPower} timeStampNanos=${device.timeStampNanos} timeStamp=${device.timeStamp} sessionId=${device.sessionId} +++++++++++++"
                    )


                    // If the library was run from a native app use the built-in Device Repository
                    GlobalScope.launch { DeviceRepository.insert(device) }

                    noCurrentDevices = false
                }
            }

            // Clear the scan results
            BtleScanCallback.mScanResults.clear()
        }
        if (noCurrentDevices) {
            GlobalScope.launch { DeviceRepository.noCurrentDevices() }
        }


    }
}