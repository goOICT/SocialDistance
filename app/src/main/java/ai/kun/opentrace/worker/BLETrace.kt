package ai.kun.opentrace.worker

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
import java.util.*
import kotlin.math.pow


object BLETrace {
    private val mBleServer : BLEServer = BLEServer()
    private val mBleClient : BLEClient = BLEClient()

    private var isInit = false
    lateinit var context : Context
    lateinit var bluetoothGattServer: BluetoothGattServer
    lateinit var bluetoothManager: BluetoothManager
    lateinit var bluetoothLeScanner: BluetoothLeScanner
    lateinit var bluetoothLeAdvertiser: BluetoothLeAdvertiser
    lateinit var alarmManager: AlarmManager


    private var uniqueID: String? = null
    fun id(context: Context): String? {
        synchronized(this) {
            if (uniqueID == null) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_UNIQUE_ID, Context.MODE_PRIVATE
                )
                uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null)
                if (uniqueID == null) {
                    uniqueID = UUID.randomUUID().toString()
                    val editor: SharedPreferences.Editor = sharedPrefs.edit()
                    editor.putString(PREF_UNIQUE_ID, uniqueID)
                    editor.commit()
                }
            }
            return uniqueID
        }
    }

    lateinit var deviceNameServiceUuid: UUID

    fun startBackground() {
        mBleServer.enable(Constants.BACKGROUND_TRACE_INTERVAL)
        mBleClient.enable(Constants.BACKGROUND_TRACE_INTERVAL)
    }

    fun stopBackground() {
        mBleServer.disable(Constants.BACKGROUND_TRACE_INTERVAL)
        mBleClient.disable(Constants.BACKGROUND_TRACE_INTERVAL)
    }

    fun startForeground() {
        mBleServer.enable(Constants.FOREGROUND_TRACE_INTERVAL)
        mBleClient.enable(Constants.FOREGROUND_TRACE_INTERVAL)
    }

    fun stopForeground() {
        mBleServer.disable(Constants.FOREGROUND_TRACE_INTERVAL)
        mBleClient.disable(Constants.FOREGROUND_TRACE_INTERVAL)
    }

    fun init(applicationContext: Context) {
        synchronized(this) {
            if (!isInit) {
                context = applicationContext
                bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothLeScanner = bluetoothManager.adapter.bluetoothLeScanner
                bluetoothGattServer = bluetoothManager.openGattServer(context, GattServerCallback)
                bluetoothLeAdvertiser = bluetoothManager.adapter.bluetoothLeAdvertiser

                alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                deviceNameServiceUuid = UUID.fromString(id(applicationContext))
                isInit = true
            }
        }
    }

    fun calculateDistance(rssi: Int, txPower: Int): Float? {
        if (txPower == -1) return null
        return 10f.pow(( 0 - txPower - rssi) / (10*RANGE_ENVIRONMENTAL)) * 10

    }

}