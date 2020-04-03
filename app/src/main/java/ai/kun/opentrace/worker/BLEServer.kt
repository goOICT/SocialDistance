package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants.BROADCAST_PERIOD
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_ECHO_UUID
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_TIME_UUID
import ai.kun.opentrace.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.BLUETOOTH_SERVICE
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.experimental.and


class BLEServer() : BroadcastReceiver(), GattServerActionListener  {
    private val TAG = "BLEServer"

    private val mDevices: List<BluetoothDevice> = ArrayList<BluetoothDevice>()
    private val mClientConfigurations: HashMap<String, ByteArray> = HashMap<String, ByteArray>()

    private lateinit var mGattServer: BluetoothGattServer
    private lateinit var mBluetoothManager: BluetoothManager
    private lateinit var mBluetoothAdapter: BluetoothAdapter
    private lateinit var mBluetoothLeAdvertiser: BluetoothLeAdvertiser
    private lateinit var mDeviceInfo: String


    override fun onReceive(context: Context, intent: Intent) {

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
    }

    fun enable(context: Context) {
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
        onReceive(context, Intent())
    }

    fun disable(context: Context) {
        stopAdvertising()
        stopServer()
        /* For now the server is on all the time
        val intent = Intent(context, BLEServer::class.java)
        val sender = PendingIntent.getBroadcast(context, 0, intent, 0)
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
         */

    }


    // GattServer
    private fun setupServer() {
        if (mGattServer.getService(SERVICE_UUID) == null) {
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
            mGattServer.addService(service)
        }
    }

    private fun stopServer() {
        if (mGattServer != null) {
            mGattServer.close()
        }
    }

    // Advertising
    private fun startAdvertising() {
        //TODO: add code to only start if it has not been started and always use the same UUID
        if (mBluetoothLeAdvertiser == null) {
            return
        }
        val uuid = UUID.randomUUID().toString()
        val substring = uuid.substring(uuid.length - 3, uuid.length)
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(BROADCAST_PERIOD)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
            .build()
        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addManufacturerData(
                1023,
                substring.toByteArray(StandardCharsets.UTF_8)
            )
            .addServiceUuid(parcelUuid)
            .build()
        mBluetoothLeAdvertiser.startAdvertising(settings, data, mAdvertiseCallback)
    }

    private fun stopAdvertising() {
        mBluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback)
    }

    private val mAdvertiseCallback: AdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            log("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            log("Peripheral advertising failed: $errorCode")
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
        val service = mGattServer.getService(SERVICE_UUID)
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
                mGattServer.notifyCharacteristicChanged(device, characteristic, confirm)
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
    override fun log(msg: String) {
        Log.d(TAG, msg)
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
        mGattServer.sendResponse(device, requestId, status, 0, null)
    }

    override fun notifyCharacteristicEcho(value: ByteArray) {
        notifyCharacteristic(value, CHARACTERISTIC_ECHO_UUID)
    }
}