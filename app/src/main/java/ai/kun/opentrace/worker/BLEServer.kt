package ai.kun.opentrace.worker

import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.BACKGROUND_TRACE_INTERVAL
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_ECHO_UUID
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_TIME_UUID
import ai.kun.opentrace.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import ai.kun.opentrace.util.Constants.SERVICE_UUID
import ai.kun.opentrace.worker.BLETrace.context
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.*
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import java.nio.charset.StandardCharsets


class BLEServer : BroadcastReceiver(), GattServerActionListener  {
    private val TAG = "BLEServer"
    private val WAKELOCK_TAG = "ai:kun:opentrace:worker:BLEServer"
    private val INTERVAL_KEY = "interval"
    private val SERVER_REQUEST_CODE = 10


    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(BLETrace) {
            // Chain the next alarm...
            enable(interval)

            GattServerCallback.serverActionListener = this
            setupServer()
            startAdvertising()
        }
        wl.release()
    }

    fun enable(interval: Int) {
        BLETrace.alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            ((System.currentTimeMillis() / interval) * interval) + interval,
            getPendingIntent(interval))
    }

    fun disable(interval: Int) {
        synchronized (BLETrace) {
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            alarmManager.cancel(getPendingIntent(interval))
        }
    }

    private fun getPendingIntent(interval: Int) : PendingIntent {
        val intent = Intent(context, BLEServer::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(context, SERVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    // GattServer
    private fun setupServer() {
/*        val userNameServiceUuid = UUID.fromString(CHARACTERISTIC_USER_UUID.toString().replaceRange(9,13, USER_SHORT_ID))
        val deviceNameServiceUuid = UUID.fromString(CHARACTERISTIC_DEVICE_UUID.toString().replaceRange(9,13, DEVICE_SHORT_ID))
        if (gattServer.getService(userNameServiceUuid) == null) {
            val userService = BluetoothGattService(
                userNameServiceUuid,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            gattServer.addService(userService)
        }
        if (gattServer.getService(deviceNameServiceUuid) == null) {
            val deviceService = BluetoothGattService(
                deviceNameServiceUuid,
                BluetoothGattService.SERVICE_TYPE_PRIMARY
            )
            gattServer.addService(deviceService)
        }*/
        if (BLETrace.bluetoothGattServer.getService(SERVICE_UUID) == null) {
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
            BLETrace.bluetoothGattServer.addService(service)
        }

    }

    private fun stopServer(gattServer: BluetoothGattServer) {
        gattServer.close()
        log("server closed.")
    }

    // Advertising
    private fun startAdvertising() {

        //TODO: replace this with device id and user id
        val uuid = "Kunai OpenTrace"
        val substring = uuid.substring(uuid.length, uuid.length)

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(Constants.BROADCAST_PERIOD.toInt())
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(true)
            .addManufacturerData(
                1023,
                substring.toByteArray(StandardCharsets.UTF_8)
            )
            //.addServiceUuid(ParcelUuid(UUID.fromString(CHARACTERISTIC_DEVICE_UUID.toString().replaceRange(9,13, DEVICE_SHORT_ID))))
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .build()
        BLETrace.bluetoothLeAdvertiser.stopAdvertising(BLEServerCallback)
        BLETrace.bluetoothLeAdvertiser.startAdvertising(settings, data, BLEServerCallback)
        //Handler().postDelayed(Runnable { stopAdvertising(bluetoothLeAdvertiser) }, Constants.BROADCAST_PERIOD)
        Log.d(TAG, ">>>>>>>>>>BLE Beacon Started")
    }

    private fun stopAdvertising(bluetoothLeAdvertiser: BluetoothLeAdvertiser) {
        synchronized(this) {
            bluetoothLeAdvertiser.stopAdvertising(BLEServerCallback)
            log("<<<<<<<<<<BLE Beacon Forced Stopped")
        }
    }

    // Gatt Server Action Listener
    override fun log(message: String) {
        Log.d(BLEServerCallback.TAG, message)
    }

    override fun addDevice(device: BluetoothDevice) {
        log("Deviced added: " + device.address)
    }

    override fun removeDevice(device: BluetoothDevice) {
        log("Deviced removed: " + device.address)
    }

    override fun addClientConfiguration(device: BluetoothDevice, value: ByteArray) {
        val deviceAddress = device.address
        BLEServerCallback.mClientConfigurations[deviceAddress] = value
    }

    override fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray
    ) {
        //mGattServer?.sendResponse(device, requestId, status, offset, value)
    }
}