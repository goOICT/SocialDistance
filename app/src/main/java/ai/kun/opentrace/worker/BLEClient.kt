package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.SCAN_PERIOD
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.os.Handler
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log


class BLEClient() : BroadcastReceiver(), GattClientActionListener  {
    private val TAG = "BLEClient"
    private val WAKELOCK_TAG = "ai:kun:opentrace:worker:BLEClient"
    private val INTERVAL_KEY = "interval"
    private val CLIENT_REQUEST_CODE = 11


    private val mScanResults: MutableMap<String, BluetoothDevice> = HashMap<String, BluetoothDevice>()

    private var mScanning = false
    private var mConnected = false
    private var mTimeInitialized = false
    private var mEchoInitialized = false

    private lateinit var mDeviceInfo: String
    private var mHandler: Handler? = null
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothLeScanner: BluetoothLeScanner
    private var mScanCallback: ScanCallback? = null
    private var mGatt: BluetoothGatt? = null

    private lateinit var mContext: Context


    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        // Chain the next alarm...
        enable(context, interval)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())

        mBluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter

        mDeviceInfo = """
            Device Info
            Name: ${mBluetoothAdapter.name}
            Address: ${mBluetoothAdapter.address}
            """.trimIndent()
        startScan()
        wl.release()
    }

    fun enable(context: Context, interval: Int) {
        mContext = context
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ((System.currentTimeMillis() / interval) * interval) + interval,
            getPendingIntent(context, interval))
    }

    fun disable(context: Context, interval: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(getPendingIntent(context, interval))
        stopScan()
    }

    private fun getPendingIntent(context: Context, interval: Int) : PendingIntent {
        val intent = Intent(context, BLEClient::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(context, CLIENT_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    // Scanning
    private fun startScan() {
        if (mScanning) {
            return
        }

        disconnectGattServer()

        mScanCallback = BtleScanCallback(mScanResults)
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner

        // Note: Filtering does not work the same (or at all) on most devices. It also is unable to
        // search for a mask or anything less than a full UUID.
        // Unless the full UUID of the server is known, manual filtering may be necessary.
        // For example, when looking for a brand of device that contains a char sequence in the UUID
        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()

        mBluetoothLeScanner.startScan(listOf(scanFilter), settings, mScanCallback)
        mHandler = Handler()
        mHandler!!.postDelayed(Runnable { stopScan() }, SCAN_PERIOD)
        mScanning = true
        log("+++++++Started scanning.")
    }

    private fun stopScan() {
        if (mScanning  && mBluetoothAdapter.isEnabled) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            scanComplete()
        }
        mScanCallback = null
        mScanning = false
        mHandler = null
        log("-------Stopped scanning.")
    }

    private fun scanComplete() {
        if (mScanResults.isEmpty()) {
            return
        }
        for (deviceAddress in mScanResults.keys) {
            val device: BluetoothDevice? = mScanResults.get(deviceAddress)
            Log.d(TAG, "------Device scanned: name=${device?.name} type=${device?.type} address=${device?.address}-------")
            //TODO: Put the results somewhere
            //TODO: Expand the results to have more information
            //TODO: Add location
        }
    }

    private class BtleScanCallback internal constructor(private val mScanResults: MutableMap<String, BluetoothDevice>) :
        ScanCallback() {
        private val TAG = "BtleScanCallback"

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
            Log.e(TAG,"BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            mScanResults[deviceAddress] = device
        }
    }

    // Gatt connection

    // Gatt connection
    private fun connectDevice(device: BluetoothDevice) {
        log("Connecting to " + device.address)
        val gattClientCallback = GattClientCallback(this)
        mGatt = device.connectGatt(mContext, false, gattClientCallback)
    }

    // Messaging
    private fun sendMessage(message: String) {
        if (!mConnected || !mEchoInitialized) {
            return
        }
        val characteristic =
            BluetoothUtils.findEchoCharacteristic(mGatt!!)
        if (characteristic == null) {
            logError("Unable to find echo characteristic.")
            disconnectGattServer()
            return
        }

        log("Sending message: $message")
        val messageBytes: ByteArray = ByteUtils.bytesFromString(message)
        if (messageBytes.size == 0) {
            logError("Unable to convert message to bytes")
            return
        }
        characteristic.value = messageBytes
        val success = mGatt!!.writeCharacteristic(characteristic)
        if (success) {
            log("Wrote: " + ByteUtils.byteArrayInHexFormat(messageBytes))
        } else {
            logError("Failed to write data")
        }
    }

    private fun requestTimestamp() {
        if (!mConnected || !mTimeInitialized) {
            return
        }
        val characteristic =
            BluetoothUtils.findTimeCharacteristic(mGatt!!)
        if (characteristic == null) {
            logError("Unable to find time charactaristic")
            return
        }
        mGatt!!.readCharacteristic(characteristic)
    }

    // Gat Client Action Listener
    override fun log(msg: String) {
        Log.d(TAG, msg)
    }

    override fun logError(msg: String) {
        Log.e(TAG,"Error: $msg")
    }

    override fun setConnected(connected: Boolean) {
        mConnected = connected
    }

    override fun initializeTime() {
        mTimeInitialized = true
    }

    override fun initializeEcho() {
        mEchoInitialized = true
    }

    override fun disconnectGattServer() {
        log("Closing Gatt connection")
        mConnected = false
        mEchoInitialized = false
        mTimeInitialized = false
        mGatt?.disconnect()
        mGatt?.close()
    }
}