package ai.kun.opentracesdk_light

import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import java.util.*

class BLEScanner: BroadcastReceiver() {

    var scanCallback: ScanCallback? = null

    companion object {
        private val handler = Handler()
        private val TAG = "BLEScanner"
        private val WAKELOCK_TAG = "ai:kun:socialdistancealarm:worker:BLEClient"
        private val INTERVAL_KEY = "interval"
        private val CLIENT_REQUEST_CODE = 11
        private val START_DELAY = 10

        fun getPendingIntent(interval: Int, context: Context): PendingIntent {
            val intent = Intent(context, BLEScanner::class.java)
            intent.putExtra(INTERVAL_KEY, interval)
            return PendingIntent.getBroadcast(
                context,
                CLIENT_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        fun enable(interval: Int, context: Context) {
            Log.i(TAG, "enabling BLEScanner")
            val alarmManager = BluetoothUtils.getAlarmManager(context)
            val pendingIntent = getPendingIntent(interval, context)
            AlarmManagerCompat.setAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + START_DELAY,
                pendingIntent);
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(TAG, "onReceive")
        if (context == null || intent == null) return

        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(TAG) {
            // Chain the next alarm...
            scheduleNextScan(context, interval)
            if (BluetoothUtils.bleIsEnabled(context)) {
                startScan(context)
            } else {
                Log.w(TAG, "Bluetooth not enabled")
            }
        }
        wl.release()
    }



    private fun scheduleNextScan(context: Context, interval: Int) {
        BluetoothUtils.getAlarmManager(context).setExact(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            getPendingIntent(interval, context))
    }

    private fun startScan(context: Context) {
        Log.i(TAG, "startScan")
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(
                ParcelUuid(UUID.fromString(Constants.ANDROID_SERVICE_STRING)),
                ParcelUuid(UUID.fromString(Constants.SERVICE_STRING_MASK))
            )
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            val adapter = BluetoothUtils.getBluetoothManager(context).adapter
            val scanner = adapter.bluetoothLeScanner

            scanCallback = object: ScanCallback() {
                override fun onScanResult(
                    callbackType: Int,
                    result: ScanResult
                ) {
                    addScanResult(result)
                }

                override fun onBatchScanResults(results: List<ScanResult>) {
                    for (result in results) {
                        addScanResult(result)
                    }
                }

                override fun onScanFailed(errorCode: Int) {
                    Log.e(TAG, "BLE Scan Failed with code $errorCode")
                }

                private fun addScanResult(result: ScanResult) {
                    Log.i(TAG, "BLE device scanned ${result.rssi}")
                    Intent().also {  intent ->
                        intent.action = Constants.INTENT_DEVICE_SCANNED
                        intent.putExtra("rssi", result.rssi)
                        context.sendBroadcast(intent)
                    }
                }
            }
            scanner.startScan(
                listOf(scanFilter),
                settings,
                scanCallback
            )

           handler.postDelayed(Runnable { stopScan(context) }, Constants.SCAN_PERIOD)
            Log.d(TAG, "+++++++Started scanning.")
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while starting scanning caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
        }
    }

    private fun stopScan(context: Context) {
        scanCallback?.let { BluetoothUtils.getBluetoothScanner(context).stopScan(it) }
    }
}
