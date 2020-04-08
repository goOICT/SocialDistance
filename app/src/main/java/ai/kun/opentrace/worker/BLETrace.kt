package ai.kun.opentrace.worker

import ai.kun.opentrace.util.Constants
import android.app.AlarmManager
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context

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
                isInit = true
            }
        }
    }

}