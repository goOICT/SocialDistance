package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.DEVICE_SHORT_ID
import ai.kun.opentrace.util.Constants.MANUFACTURE_SUBSTRING
import ai.kun.opentrace.util.Constants.SCAN_PERIOD
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import ai.kun.opentrace.util.Constants.USER_SHORT_ID
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import java.nio.charset.StandardCharsets


class BLEClient : BroadcastReceiver() {
    private val TAG = "BLEClient"
    private val WAKELOCK_TAG = "ai:kun:opentrace:worker:BLEClient"
    private val INTERVAL_KEY = "interval"
    private val CLIENT_REQUEST_CODE = 11

    private var mScanning = false
    private var mConnected = false

    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(BLETrace) {
            // Chain the next alarm...
            enable(interval)
            startScan()
        }
        wl.release()
    }

    private fun getAdapter(context: Context): BluetoothAdapter? {
        return (context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }

    fun enable(interval: Int) {
        BLETrace.alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ((System.currentTimeMillis() / interval) * interval) + interval,
            getPendingIntent(interval)
        )
    }

    fun disable(interval: Int) {
        synchronized(BLETrace) {
            BLETrace.alarmManager.cancel(getPendingIntent(interval))
            stopScan()
        }
    }

    private fun getPendingIntent(interval: Int): PendingIntent {
        val intent = Intent(BLETrace.context, BLEClient::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(
            BLETrace.context,
            CLIENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    // Scanning
    private fun startScan() {
        if (mScanning) {
            Log.w(TAG,"Already scanning")
            return
        }

        val scanFilter = ScanFilter.Builder()
            .setManufacturerData(
                Constants.MANUFACTURE_ID,
                MANUFACTURE_SUBSTRING.toByteArray(StandardCharsets.UTF_8))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
            .build()

        BLETrace.bluetoothLeScanner.startScan(listOf(scanFilter), settings, BtleScanCallback)
        BtleScanCallback.handler.postDelayed(Runnable { stopScan() }, SCAN_PERIOD)
        mScanning = true
        Log.d(TAG, "+++++++Started scanning.")
    }

    private fun stopScan() {

        synchronized(BLETrace) {
            if (mScanning && BLETrace.bluetoothManager.adapter.isEnabled) {
                BLETrace.bluetoothLeScanner.stopScan(BtleScanCallback)
                scanComplete()
            }
            mScanning = false
        }
        Log.d(TAG, "-------Stopped scanning.")
    }

    private fun scanComplete() {
        if (BtleScanCallback.mScanResults.isEmpty()) {
            return
        }
        for (deviceAddress in BtleScanCallback.mScanResults.keys) {
            val result: ScanResult? = BtleScanCallback.mScanResults.get(deviceAddress)
            result?.let { scanResult ->
                var uuid: ParcelUuid? = scanResult.scanRecord?.serviceUuids?.get(0)
                var rssi: Int = scanResult.rssi
                var txPower: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    scanResult.txPower
                } else {
                    -1
                }
                val distance = BLETrace.calculateDistance(rssi, txPower)
                var timeStampNanos: Long = scanResult.timestampNanos
                var sessionId = deviceAddress

                Log.d(TAG, "+++++++++++++ Traced: device=$uuid distance=$distance rssi=$rssi txPower=$txPower timeStampNanos=$timeStampNanos sessionId=$sessionId +++++++++++++")

                //TODO: Add this to firebase!!!
            }
        }

        // Clear the scan results
        BtleScanCallback.mScanResults.clear()
    }
}