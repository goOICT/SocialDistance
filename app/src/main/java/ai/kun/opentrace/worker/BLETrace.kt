package ai.kun.opentrace.worker

import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.dao.DeviceDao
import ai.kun.opentrace.dao.DeviceRepository
import ai.kun.opentrace.dao.DeviceRoomDatabase
import ai.kun.opentrace.ui.api.FirebaseOpenTraceApi
import ai.kun.opentrace.util.Constants
import ai.kun.opentrace.util.Constants.PREF_UNIQUE_ID
import ai.kun.opentrace.util.Constants.RANGE_ENVIRONMENTAL
import android.app.AlarmManager
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.GlobalScope
import java.util.*
import kotlin.math.pow


object BLETrace {
    private val mBleServer : BLEServer = BLEServer()
    private val mBleClient : BLEClient = BLEClient()

    private var isInit = false
    lateinit var context : Context
    lateinit var bluetoothGattServer : BluetoothGattServer
    lateinit var bluetoothManager : BluetoothManager
    lateinit var bluetoothLeScanner : BluetoothLeScanner
    lateinit var bluetoothLeAdvertiser : BluetoothLeAdvertiser
    lateinit var alarmManager : AlarmManager

    private lateinit var deviceDao: DeviceDao
    lateinit var deviceRepository: DeviceRepository

    public var isBackground : Boolean = true

    public var uniqueId : String?
        get() {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE
                )
                return sharedPrefs.getString(PREF_UNIQUE_ID, null)
            }
        }
        set(value) {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE
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

    fun startBackground() {
        if (isBleEnabled()) {
            synchronized(this) {
                isBackground = true
                mBleServer.enable(Constants.BACKGROUND_TRACE_INTERVAL)
                mBleClient.enable(Constants.BACKGROUND_TRACE_INTERVAL)
            }
        }
    }

    fun stop() {
        synchronized(this) {
            if (isBackground) {
                stopBackground()
            } else {
                stopForeground()
            }
        }
    }

    private fun stopBackground() {
        if (isBleEnabled()) {
            mBleServer.disable(Constants.BACKGROUND_TRACE_INTERVAL)
            mBleClient.disable(Constants.BACKGROUND_TRACE_INTERVAL)
        }
    }

    fun startForeground() {
        if (isBleEnabled()) {
            mBleServer.enable(Constants.FOREGROUND_TRACE_INTERVAL)
            mBleClient.enable(Constants.FOREGROUND_TRACE_INTERVAL)
        }
    }

    private fun stopForeground() {
        if (isBleEnabled()) {
            mBleServer.disable(Constants.FOREGROUND_TRACE_INTERVAL)
            mBleClient.disable(Constants.FOREGROUND_TRACE_INTERVAL)
        }
    }

    fun isBleEnabled() : Boolean {
        // TODO: this needs to be more complicated and test for permissions and if bluetooth is on, etc.
        return uniqueId != null && isInit
    }

    fun init(applicationContext: Context) {
        synchronized(this) {
            context = applicationContext
            deviceDao = DeviceRoomDatabase.getDatabase(context, GlobalScope).deviceDao()
            deviceRepository = DeviceRepository(deviceDao)

            if (!isInit && uniqueId != null) {
                bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
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
        return 10f.pow((0 - txPower - rssi) / (10 * RANGE_ENVIRONMENTAL)) * 10

    }

}