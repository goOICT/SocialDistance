package ai.kun.opentrace.worker

import ai.kun.opentrace.dao.DeviceDao
import ai.kun.opentrace.dao.DeviceRepository
import ai.kun.opentrace.dao.DeviceRoomDatabase
import ai.kun.opentrace.ui.api.FirebaseOpenTraceApi
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.PREF_FILE_NAME
import ai.kun.opentrace.util.Constants.PREF_UNIQUE_ID
import ai.kun.opentrace.util.Constants.RANGE_ENVIRONMENTAL
import android.app.AlarmManager
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import kotlinx.coroutines.GlobalScope
import java.util.*
import kotlin.math.pow


object BLETrace {
    private val mBleServer : BLEServer = BLEServer()
    private val mBleClient : BLEClient = BLEClient()
    private val TAG = "BLETrace"

    private var isInit = false
    lateinit var context : Context
    lateinit var bluetoothGattServer : BluetoothGattServer
    lateinit var bluetoothManager : BluetoothManager
    lateinit var bluetoothLeScanner : BluetoothLeScanner
    lateinit var bluetoothLeAdvertiser : BluetoothLeAdvertiser
    lateinit var alarmManager : AlarmManager

    public var isBackground : Boolean = true
    public var isStarted: Boolean = false

    public var uniqueId : String?
        get() {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                return sharedPrefs.getString(PREF_UNIQUE_ID, null)
            }
        }
        set(value) {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                if (value != null) {
                    editor.putString(PREF_UNIQUE_ID, value)
                    editor.commit()
                    init(context)
                    deviceNameServiceUuid = UUID.nameUUIDFromBytes(value?.toByteArray())
                    FirebaseOpenTraceApi().setDeviceUuid(deviceUuid.toString())
                } else {
                    editor.remove(PREF_UNIQUE_ID)
                    editor.commit()
                    stop()
                }
            }
        }

    public var deviceUuid : String? = null
        get() = if (uniqueId == null) {
            null
        } else {
            UUID.nameUUIDFromBytes(uniqueId?.toByteArray()).toString()
        }

    lateinit var deviceNameServiceUuid: UUID

    fun start(startingBackground: Boolean) {
        synchronized(this) {
            if (isStarted) stop()
            if (startingBackground) startBackground() else startForeground()
        }
    }

    fun stop() {
        synchronized(this) {
            if (isBackground) stopBackground() else stopForeground()
        }
    }

    /*
     * The following methods deal with the problem that your intent that you use to stop
     * an alarm manager has to be identical to the intent that you used to stop it.  So
     * for that to be true you have to cancel the alarm with the correct argument for
     * background vs foreground, and thus a bunch of code...
     */
    private fun startBackground() {
        if (isEnabled()) {
            isBackground = true
            isStarted = true
            mBleServer.enable(Constants.BACKGROUND_TRACE_INTERVAL)
            mBleClient.enable(Constants.BACKGROUND_TRACE_INTERVAL)
        }
    }

    private fun startForeground() {
        if (isEnabled()) {
            isBackground = false
            isStarted = true
            mBleServer.enable(Constants.FOREGROUND_TRACE_INTERVAL)
            mBleClient.enable(Constants.FOREGROUND_TRACE_INTERVAL)
        }
    }

    private fun stopBackground() {
        if (isEnabled()) {
            mBleServer.disable(Constants.BACKGROUND_TRACE_INTERVAL)
            mBleClient.disable(Constants.BACKGROUND_TRACE_INTERVAL)
        }
    }

    private fun stopForeground() {
        if (isEnabled()) {
            mBleServer.disable(Constants.FOREGROUND_TRACE_INTERVAL)
            mBleClient.disable(Constants.FOREGROUND_TRACE_INTERVAL)
        }
    }

    fun isEnabled() : Boolean {
        if (uniqueId == null || !bluetoothManager.adapter.isEnabled()) return false

        if (!isInit) init(context) // If bluetooth was off we need to complete the init

        return isInit  // && isLocationEnabled() Location doesn't need to be on
    }

    fun init(applicationContext: Context) {
        synchronized(this) {
            context = applicationContext
            DeviceRepository.init(applicationContext)

            if (!isInit && uniqueId != null) {
                bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                if (!bluetoothManager.adapter.isEnabled()) return // bail if bluetooth isn't on
                bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
                bluetoothGattServer = bluetoothManager.openGattServer(context, GattServerCallback)
                bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                deviceNameServiceUuid = UUID.nameUUIDFromBytes(uniqueId?.toByteArray())
                isInit = true
            }
        }
    }

    fun calculateDistance(rssi: Int, txPower: Int): Float? {
        if (txPower == -1) return null
        return 10f.pow(((0 - txPower - rssi).toFloat()).div(10 * RANGE_ENVIRONMENTAL)) * 100

    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}