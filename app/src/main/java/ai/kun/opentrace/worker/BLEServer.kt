package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.BACKGROUND_TRACE_INTERVAL
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_ECHO_UUID
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_TIME_UUID
import ai.kun.opentrace.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.os.Handler
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.and


class BLEServer() : BroadcastReceiver(), GattServerActionListener  {
    private val TAG = "BLEServer"
    private val WAKELOCK_TAG = "ai:kun:opentrace:worker:BLEServer"
    private val INTERVAL_KEY = "interval"
    private val SERVER_REQUEST_CODE = 10

    private val mDevices: List<BluetoothDevice> = ArrayList<BluetoothDevice>()
    private val mClientConfigurations: HashMap<String, ByteArray> = HashMap<String, ByteArray>()


    private var mHandler: Handler? = null
    private var mGattServer: BluetoothGattServer? = null
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private lateinit var mDeviceInfo: String


    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, BACKGROUND_TRACE_INTERVAL)
        // Chain the next alarm...
        enable(context, interval)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())

        mBluetoothManager = context.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = mBluetoothManager.adapter
        mBluetoothLeAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
        val gattServerCallback = GattServerCallback(this)
        mGattServer = mBluetoothManager.openGattServer(context, gattServerCallback)

        mDeviceInfo = """
            Device Info
            Name: ${mBluetoothAdapter.name}
            Address: ${mBluetoothAdapter.address}
            """.trimIndent()

        setupServer()
        startAdvertising()

        wl.release()
    }

    fun enable(context: Context, interval: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            ((System.currentTimeMillis() / interval) * interval) + interval,
            getPendingIntent(context, interval))
    }

    fun disable(context: Context, interval: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(getPendingIntent(context, interval))
        stopAdvertising()
        stopServer()
    }

    private fun getPendingIntent(context: Context, interval: Int) : PendingIntent {
        val intent = Intent(context, BLEServer::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(context, SERVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    // GattServer
    private fun setupServer() {
        if (mGattServer?.getService(SERVICE_UUID) == null) {
            val service = BluetoothGattService(
                SERVICE_UUID,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )

            // Write characteristic
            val writeCharacteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_ECHO_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,  // Somehow this is not necessary, the client can still enable notifications
                //                        | BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE
            )

            // Characteristic with Descriptor
            val notifyCharacteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_TIME_UUID,  // Somehow this is not necessary, the client can still enable notifications
                //                BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                0,
                0
            )
            val clientConfigurationDescriptor = BluetoothGattDescriptor(
                CLIENT_CONFIGURATION_DESCRIPTOR_UUID,
                BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
            )
            clientConfigurationDescriptor.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            notifyCharacteristic.addDescriptor(clientConfigurationDescriptor)
            service.addCharacteristic(writeCharacteristic)
            service.addCharacteristic(notifyCharacteristic)
            mGattServer?.addService(service)
        }
    }

    private fun stopServer() {
        mGattServer?.close()
        log("server closed.")
    }

    // Advertising
    private fun startAdvertising() {
        //TODO: add code to only start if it has not been started and always use the same UUID
        if (mBluetoothLeAdvertiser == null) {
            return
        }
        //TODO: replace this with device id and user id
        val uuid = UUID.randomUUID().toString()
        val substring = uuid.substring(uuid.length - 3, uuid.length)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addManufacturerData(
                1023,
                substring.toByteArray(StandardCharsets.UTF_8)
            )
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            //.addServiceData(ParcelUuid(USER_UUID), "Some User".toByteArray(Charsets.UTF_8))
            .build()
        mBluetoothLeAdvertiser!!.startAdvertising(settings, data, mAdvertiseCallback)
        mHandler = Handler()
        mHandler!!.postDelayed(Runnable { stopAdvertising() }, Constants.BROADCAST_PERIOD)
        log("Started scanning.")
        Log.d(TAG, ">>>>>>>>>>BLE Beacon Started")
    }

    private fun stopAdvertising() {
        mBluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback)
        mHandler = null
        log("<<<<<<<<<<BLE Beacon Forced Stopped")
    }

    private val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            log("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Log.e(TAG,"Peripheral advertising failed: $errorCode")
        }
    }

    // Notifications
    private fun notifyCharacteristicTime(value: ByteArray) {
        notifyCharacteristic(value, CHARACTERISTIC_TIME_UUID)
    }

    private fun notifyCharacteristic(
        value: ByteArray,
        uuid: UUID
    ) {
        val service = mGattServer!!.getService(SERVICE_UUID)
        val characteristic = service.getCharacteristic(uuid)
        log(
            "Notifying characteristic " + characteristic.uuid.toString()
                    + ", new value: " + ByteUtils.byteArrayInHexFormat(value)
        )
        characteristic.value = value
        // Indications require confirmation, notifications do not
        val confirm: Boolean = BluetoothUtils.requiresConfirmation(characteristic)
        for (device in mDevices) {
            if (clientEnabledNotifications(device, characteristic)) {
                mGattServer!!.notifyCharacteristicChanged(device, characteristic, confirm)
            }
        }
    }

    private fun clientEnabledNotifications(
        device: BluetoothDevice,
        characteristic: BluetoothGattCharacteristic
    ): Boolean {
        val descriptorList =
            characteristic.descriptors
        val descriptor: BluetoothGattDescriptor =
            BluetoothUtils.findClientConfigurationDescriptor(descriptorList)
                ?: // There is no client configuration descriptor, treat as true
                return true
        val deviceAddress = device.address
        val clientConfiguration = mClientConfigurations[deviceAddress]
            ?: // Descriptor has not been set
            return false
        val notificationEnabled =
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        return clientConfiguration.size == notificationEnabled.size
                && clientConfiguration[0] and notificationEnabled[0] == notificationEnabled[0]
                && clientConfiguration[1] and notificationEnabled[1] == notificationEnabled[1]
    }

    // Characteristic operations

    // Characteristic operations
    private fun getTimestampBytes(): ByteArray {
        @SuppressLint("SimpleDateFormat") val dateFormat =
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val timestamp = dateFormat.format(Date())
        return ByteUtils.bytesFromString(timestamp)
    }

    private fun sendTimestamp() {
        val timestampBytes = getTimestampBytes()
        notifyCharacteristicTime(timestampBytes)
    }

    // Gatt Server Action Listener
    override fun log(message: String) {
        Log.d(TAG, message)
    }

    override fun addDevice(device: BluetoothDevice) {
        log("Deviced added: " + device.address)
    }

    override fun removeDevice(device: BluetoothDevice) {
        log("Deviced removed: " + device.address)
    }

    override fun addClientConfiguration(device: BluetoothDevice, value: ByteArray) {
        val deviceAddress = device.address
        mClientConfigurations[deviceAddress] = value
    }

    override fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray
    ) {
        mGattServer!!.sendResponse(device, requestId, status, 0, null)
    }

    override fun notifyCharacteristicEcho(value: ByteArray) {
        notifyCharacteristic(value, CHARACTERISTIC_ECHO_UUID)
    }
}