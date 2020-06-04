package ai.kun.opentracesdk_light

import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.os.PowerManager
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import java.nio.charset.StandardCharsets
import java.util.*

class BLEAdvertiser: BroadcastReceiver() {

    companion object {
        private val TAG = "BLEAdvertiser"
        private val WAKELOCK_TAG = "ai:kun:socialdistancealarm:worker:BLEServer"
        private val INTERVAL_KEY = "interval"
        private val SERVER_REQUEST_CODE = 10
        private val START_DELAY = 10

        fun enable(interval: Int, context: Context) {
            val alarmManager = BluetoothUtils.getAlarmManager(context)
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + START_DELAY,
                getPendingIntent(interval, context))
        }

        private fun getPendingIntent(interval: Int, context: Context) : PendingIntent {
            val intent = Intent(context, BLEAdvertiser::class.java)
            intent.putExtra(INTERVAL_KEY, interval)
            return PendingIntent.getBroadcast(context, SERVER_REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }


    private val callback = object: AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            Log.i(TAG, "onStartFailure")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            Log.i(TAG, "onStartSuccess")
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        val interval = intent.getIntExtra(INTERVAL_KEY, Constants.BACKGROUND_TRACE_INTERVAL)
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKELOCK_TAG)
        wl.acquire(interval.toLong())
        synchronized(TAG) {
            next(context, interval)

            if (BluetoothUtils.bleIsEnabled(context)) {
                // TODO: change it to dynamic ID?
                startAdvertising(context, UUID.fromString(Constants.ANDROID_SERVICE_STRING))
            }
        }
        wl.release()
    }


    fun next(context: Context, interval: Int) {
        val alarmManager = BluetoothUtils.getAlarmManager(context)
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager,
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + interval,
            getPendingIntent(interval, context))
    }


    fun disable(interval: Int, context: Context) {
        synchronized (TAG) {
            BluetoothUtils.getAlarmManager(context).cancel(getPendingIntent(interval, context))
            stopAdvertising(context)
        }
    }

    // Advertising
    private fun startAdvertising(context: Context, uuid: UUID) {
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
                    Constants.MANUFACTURE_ID,
                    Constants.MANUFACTURE_SUBSTRING.toByteArray(StandardCharsets.UTF_8)
                )
                .addServiceUuid(ParcelUuid(uuid))
                .build()

            val advertiser = BluetoothUtils.getBluetoothAdvertiser(context)
            advertiser.stopAdvertising(callback)
            advertiser.startAdvertising(settings, data, callback)
            Log.d(TAG, ">>>>>>>>>>BLE Beacon Started UUID: $uuid")
        } catch (exception: Exception) {
            val msg = " ${exception::class.qualifiedName} while starting advertising caused by ${exception.localizedMessage}"
            Log.e(TAG, msg)
        }
    }

    private fun stopAdvertising(context: Context) {
        synchronized(this) {
            try {
                val advertiser = BluetoothUtils.getBluetoothAdvertiser(context)

                advertiser.stopAdvertising(callback)
            }catch (exception: Exception) {
                val msg = " ${exception::class.qualifiedName} while stopping advertising caused by ${exception.localizedMessage}"
                Log.e(TAG, msg)
            }
        }
    }
}