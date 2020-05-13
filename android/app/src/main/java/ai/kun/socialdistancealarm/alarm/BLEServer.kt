package ai.kun.socialdistancealarm.alarm

import ai.kun.socialdistancealarm.alarm.BLETrace.getAlarmManager
import ai.kun.socialdistancealarm.util.Constants.BACKGROUND_TRACE_INTERVAL
import ai.kun.socialdistancealarm.util.Constants.MANUFACTURE_ID
import ai.kun.socialdistancealarm.util.Constants.MANUFACTURE_SUBSTRING
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.nio.charset.StandardCharsets
import java.util.*


class BLEServer : BroadcastReceiver(), GattServerActionListener  {
    private val TAG = "BLEServer"
    private val WAKELOCK_TAG = "ai:kun:socialdistancealarm:worker:BLEServer"
    private val INTERVAL_KEY = "interval"
    private val SERVER_REQUEST_CODE = 10
    private val START_DELAY = 10

    lateinit var appContext: Context


    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(BLETrace) {
            // Chain the next alarm...
            appContext = context.applicationContext
            BLETrace.init(appContext)
            next(interval)

            GattServerCallback.serverActionListener = this
            if (BLETrace.isEnabled()) {
                setupServer()
                startAdvertising(BLEServerCallbackDeviceName, BLETrace.deviceNameServiceUuid)
            }
        }
        wl.release()
    }

    fun next(interval: Int) {
        getAlarmManager(appContext).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            getPendingIntent(interval, appContext))
    }

    fun enable(interval: Int, context: Context) {
        this.appContext = context.applicationContext
        getAlarmManager(appContext).setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + START_DELAY,
            getPendingIntent(interval, appContext))
    }

    fun disable(interval: Int, context: Context) {
        synchronized (BLETrace) {
            this.appContext = context.applicationContext
            val alarmManager = getAlarmManager(appContext)

            alarmManager.cancel(getPendingIntent(interval, appContext))

            stopAdvertising()
        }
    }

    private fun getPendingIntent(interval: Int, context: Context) : PendingIntent {
        val intent = Intent(context, BLEServer::class.java)
        intent.putExtra(INTERVAL_KEY, interval)
        return PendingIntent.getBroadcast(context, SERVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    // GattServer
    private fun setupServer() {
        try {
            if (BLETrace.bluetoothGattServer!!.getService(BLETrace.deviceNameServiceUuid) == null) {
                val deviceService = BluetoothGattService(
                    BLETrace.deviceNameServiceUuid,
                    BluetoothGattService.SERVICE_TYPE_PRIMARY
                )
                BLETrace.bluetoothGattServer!!.addService(deviceService)
            }
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while setting up the server caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
            FirebaseCrashlytics.getInstance().log(TAG + msg)
        }
    }

    private fun stopServer(gattServer: BluetoothGattServer) {
        gattServer.close()
        log("server closed.")
    }

    // Advertising
    private fun startAdvertising(callback: AdvertiseCallback, uuid: UUID) {
        try {
            val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .build()

            val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(true)
                .addManufacturerData(
                    MANUFACTURE_ID,
                    MANUFACTURE_SUBSTRING.toByteArray(StandardCharsets.UTF_8)
                )
                .addServiceUuid(ParcelUuid(uuid))
                .build()

            BLETrace.bluetoothLeAdvertiser!!.stopAdvertising(callback)
            BLETrace.bluetoothLeAdvertiser!!.startAdvertising(settings, data, callback)
            Log.d(TAG, ">>>>>>>>>>BLE Beacon Started")
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while starting advertising caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
            FirebaseCrashlytics.getInstance().log(TAG + msg)
        }
    }

    private fun stopAdvertising() {
        synchronized(this) {
            try {
                BLETrace.bluetoothLeAdvertiser?.stopAdvertising((BLEServerCallbackDeviceName))
                log("<<<<<<<<<<BLE Beacon Forced Stopped")
            }catch (exception: Exception) {
                val msg = " ${exception::class.qualifiedName} while stopping advertising caused by ${exception.localizedMessage}"
                Log.e(TAG, msg)
                FirebaseCrashlytics.getInstance().log(TAG + msg)
            }
        }
    }

    // Gatt Server Action Listener
    override fun log(message: String) {
        Log.d(BLEServerCallbackDeviceName.TAG, message)
    }

    override fun addDevice(device: BluetoothDevice) {
        log("Deviced added: " + device.address)
    }

    override fun removeDevice(device: BluetoothDevice) {
        log("Deviced removed: " + device.address)
    }

    override fun addClientConfiguration(device: BluetoothDevice, value: ByteArray) {
        val deviceAddress = device.address
        BLEServerCallbackDeviceName.mClientConfigurations[deviceAddress] = value
    }

    override fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray
    ) {
        try {
            BLETrace.bluetoothGattServer!!.sendResponse(device, requestId, status, offset, value)
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while sending a response caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
            FirebaseCrashlytics.getInstance().log(TAG + msg)
        }
    }
}