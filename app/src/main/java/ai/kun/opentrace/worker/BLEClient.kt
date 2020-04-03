package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants.SCAN_PERIOD
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.ParcelUuid
import android.util.Log
import androidx.core.app.ActivityCompat.requestPermissions


class BLEClient() : BroadcastReceiver(), GattClientActionListener  {
    private val TAG = "BLEClient"

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2

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

    private lateinit var mMainActivity: Activity


    override fun onReceive(context: Context, intent: Intent) {
        mBluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter

        mDeviceInfo = """
            Device Info
            Name: ${mBluetoothAdapter.name}
            Address: ${mBluetoothAdapter.address}
            """.trimIndent()
        startScan()
    }

    fun enable(activity: Activity) {
        mMainActivity = activity
        /* For now the server is on all the time, but later we should test to see if we can
         * save battery life by turning it on only when we are scanning.
        val am =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val i = Intent(context, BLEServer::class.java)
        val pi = PendingIntent.getBroadcast(context, 0, i, 0)
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            1000 * 60 * 10.toLong(),
            pi
        ) // Millisec * Second * Minute
        */
        onReceive(activity.applicationContext, Intent())
    }

    fun disable(context: Context) {

        /* For now the server is on all the time
        val intent = Intent(context, BLEServer::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
         */

    }


    // Scanning
    private fun startScan() {
        if (!hasPermissions() || mScanning) {
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
        log("Started scanning.")
    }

    private fun stopScan() {
        if (mScanning  && mBluetoothAdapter.isEnabled) {
            mBluetoothLeScanner.stopScan(mScanCallback)
            scanComplete()
        }
        mScanCallback = null
        mScanning = false
        mHandler = null
        log("Stopped scanning.")
    }

    private fun scanComplete() {
        if (mScanResults.isEmpty()) {
            return
        }
        for (deviceAddress in mScanResults.keys) {
            val device: BluetoothDevice? = mScanResults.get(deviceAddress)
            //TODO: Put the results somewhere
        }
    }

    private fun hasPermissions(): Boolean {
        if (!mBluetoothAdapter.isEnabled) {
            requestBluetoothEnable()
            return false
        } else if (!hasLocationPermissions()) {
            requestLocationPermission()
            return false
        }
        return true
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        mMainActivity.startActivityForResult(
            enableBtIntent,
            REQUEST_ENABLE_BT
        )
        log("Requested user enables Bluetooth. Try starting the scan again.")
    }


    private fun hasLocationPermissions(): Boolean {
        return mMainActivity.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) === PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        requestPermissions(mMainActivity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_FINE_LOCATION
        )
        log("Requested user enable Location. Try starting the scan again.")
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
        mGatt = device.connectGatt(mMainActivity, false, gattClientCallback)
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