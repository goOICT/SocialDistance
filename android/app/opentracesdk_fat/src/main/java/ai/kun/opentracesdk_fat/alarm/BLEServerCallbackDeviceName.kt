package ai.kun.opentracesdk_fat.alarm

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.util.Log

object BLEServerCallbackDeviceName: AdvertiseCallback() {
    const val TAG = "BLEServerAdCallback"


    val mDevices: List<BluetoothDevice> = ArrayList<BluetoothDevice>()
    val mClientConfigurations: HashMap<String, ByteArray> = HashMap<String, ByteArray>()

    override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
        Log.d(TAG,"Peripheral advertising started.")
    }

    override fun onStartFailure(errorCode: Int) {
        Log.e(TAG,"Peripheral advertising failed: $errorCode")
    }
}
