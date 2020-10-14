package ai.kun.opentracesdk_fat.alarm

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.util.Log

/**
 * We don't really use this but the code is preserve in case we want devices to connect.
 */
object BLEServerCallbackDeviceName: AdvertiseCallback() {
    const val TAG = "BLEServerAdCallback"


    val mDevices: List<BluetoothDevice> = ArrayList<BluetoothDevice>()
    val mClientConfigurations: HashMap<String, ByteArray> = HashMap<String, ByteArray>()

    /**
     * Log advertising start success.
     *
     * @param settingsInEffect not used
     */
    override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
        Log.d(TAG,"Peripheral advertising started.")
    }

    /**
     * Log advertising start failure.
     *
     * @param settingsInEffect not used
     */
    override fun onStartFailure(errorCode: Int) {
        Log.e(TAG,"Peripheral advertising failed: $errorCode")
    }
}
