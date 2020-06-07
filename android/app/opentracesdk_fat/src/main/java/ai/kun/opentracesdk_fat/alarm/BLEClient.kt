package ai.kun.opentracesdk_fat.alarm

import ai.kun.opentracesdk_fat.BLETrace
import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import ai.kun.opentracesdk_fat.util.Constants
import ai.kun.opentracesdk_fat.util.Constants.ANDROID_PREFIX
import ai.kun.opentracesdk_fat.util.Constants.IOS_PREFIX
import ai.kun.opentracesdk_fat.util.Constants.SCAN_PERIOD
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


class BLEClient : BroadcastReceiver() {
    private val TAG = "BLEClient"
    private val WAKELOCK_TAG = "ai:kun:socialdistancealarm:worker:BLEClient"
    private val INTERVAL_KEY = "interval"
    private val CLIENT_REQUEST_CODE = 11
    private val START_DELAY = 10

    private var mScanning = false
    private var mConnected = false

    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(BLETrace) {
            // Chain the next alarm...
            BLETrace.init(context.applicationContext)
            next(interval, context.applicationContext)
            if (BLETrace.isEnabled()) startScan(context.applicationContext)
        }
        wl.release()
    }

    fun next(interval: Int, context: Context) {
        BLETrace.getAlarmManager(context).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            getPendingIntent(interval, context)
        )
    }

    fun enable(interval: Int, context: Context) {
        BLETrace.getAlarmManager(context).setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + START_DELAY,
            getPendingIntent(interval, context)
        )
    }

    fun disable(interval: Int, context: Context) {
        synchronized(BLETrace) {
            BLETrace.getAlarmManager(context).cancel(getPendingIntent(interval, context))
            stopScan(context)
        }
    }

    private fun getPendingIntent(interval: Int, context: Context): PendingIntent {
        val intent = Intent(context, BLEClient::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(
            context,
            CLIENT_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }


    // Scanning
    private fun startScan(context: Context) {
        if (mScanning) {
            Log.w(TAG,"Already scanning")
            return
        }

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString(Constants.ANDROID_SERVICE_STRING)),
                            ParcelUuid(UUID.fromString(Constants.SERVICE_STRING_MASK)))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        try {
            BLETrace.bluetoothLeScanner!!.startScan(emptyList(), settings, BtleScanCallback)
            BtleScanCallback.handler.postDelayed(Runnable { stopScan(context) }, SCAN_PERIOD)
            mScanning = true
            Log.d(TAG, "+++++++Started scanning.")
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while starting scanning caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
        }
    }

    private fun stopScan(context: Context) {

        synchronized(BLETrace) {
            try {
                if (mScanning && BLETrace.bluetoothManager!!.adapter.isEnabled) {
                    BLETrace.bluetoothLeScanner!!.stopScan(BtleScanCallback)
                    scanComplete()
                }

            } catch (exception: Exception) {
                val msg = " ${exception::class.qualifiedName} while stopping scanning caused by ${exception.localizedMessage}"
                Log.e(TAG, msg)
            }
            mScanning = false
        }
        Log.d(TAG, "-------Stopped scanning.")
    }

    private fun scanComplete() {

        var noCurrentDevices = true
        if (!BtleScanCallback.mScanResults.isEmpty()) {
            for (deviceAddress in BtleScanCallback.mScanResults.keys) {
                val result: ScanResult? = BtleScanCallback.mScanResults.get(deviceAddress)
                result?.let { scanResult ->
                    var uuid: ParcelUuid? = scanResult.scanRecord?.serviceUuids?.get(0)
                    var isAndroid = (uuid.toString().startsWith(ANDROID_PREFIX))

                    // Only record devices where the UUID is one from our app...
                    if (isAndroid || uuid.toString().startsWith(IOS_PREFIX)) {
                        var rssi: Int = scanResult.rssi
                        var txPower: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            scanResult.txPower
                        } else {
                            -1
                        }
                        var timeStampNanos: Long = scanResult.timestampNanos
                        val timeStamp: Long = System.currentTimeMillis()
                        var sessionId = deviceAddress

                        Log.d(
                            TAG,
                            "+++++++++++++ Traced: device=$uuid  rssi=$rssi txPower=$txPower timeStampNanos=$timeStampNanos timeStamp=$timeStamp sessionId=$sessionId +++++++++++++"
                        )
                        val device = Device(
                            uuid.toString(),
                            null,
                            rssi,
                            txPower,
                            timeStampNanos,
                            timeStamp,
                            sessionId,
                            BLETrace.isTeamMember(uuid.toString()),
                            isAndroid
                        )
                        GlobalScope.launch { DeviceRepository.insert(device) }
                        noCurrentDevices = false
                    }
                }
            }

            // Clear the scan results
            BtleScanCallback.mScanResults.clear()
        }
        if (noCurrentDevices){
            GlobalScope.launch { DeviceRepository.noCurrentDevices() }
        }


    }
}