package ai.kun.opentracesdk_fat.alarm

import android.bluetooth.*
import ai.kun.opentracesdk_fat.util.BluetoothUtils
import ai.kun.opentracesdk_fat.util.ByteUtils

/**
 * This class is not currently used, but it supports connection from a device.  At some point we
 * though it might be useful, but we opted not to have the devices connect with each other.
 *
 * @property mClientActionListener
 */
class GattClientCallback(private val mClientActionListener: GattClientActionListener) :
    BluetoothGattCallback() {
    private val TAG = "GattClientCallback"

    override fun onConnectionStateChange(
        gatt: BluetoothGatt,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(gatt, status, newState)
        mClientActionListener.log("onConnectionStateChange newState: $newState")
        if (status == BluetoothGatt.GATT_FAILURE) {
            mClientActionListener.logError("Connection Gatt failure status $status")
            mClientActionListener.disconnectGattServer()
            return
        } else if (status != BluetoothGatt.GATT_SUCCESS) {
            // handle anything not SUCCESS as failure
            mClientActionListener.logError("Connection not GATT sucess status $status")
            //mClientActionListener.disconnectGattServer()
            return
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mClientActionListener.log("Connected to device " + gatt.device.address)
            mClientActionListener.setConnected(true)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mClientActionListener.log("Disconnected from device")
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Device service discovery unsuccessful, status $status")
            return
        }
        val serviceList = gatt.services
        val service = BluetoothUtils.findService(serviceList)
        var deviceUUIDString: String? = null
        var userUUIDString: String? = null

        if (service == null) {
            mClientActionListener.logError("BLETrace Service not found")
            return
        } else {
            val characteristicList =
                service.characteristics
            for (characteristic in characteristicList) {
                val uuidString = characteristic.uuid.toString()
                if (uuidString.regionMatches(9, "0000", 0, 4, true)) {
                    // It's the device UUID
                    deviceUUIDString = uuidString
                } else {
                    // It's the user UUID
                    userUUIDString = uuidString
                }
            }
            if (deviceUUIDString != null && userUUIDString != null) {
                mClientActionListener.log("!!!!!!!!!!!!!! Traced user: $userUUIDString device: $deviceUUIDString !!!!!!!!!!!!!!!!!!!!")
            } else {
                mClientActionListener.logError("Invalid trace: user: $userUUIDString device: $deviceUUIDString")
            }
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Characteristic read successfully")
            readCharacteristic(characteristic)
        } else {
            mClientActionListener.logError("Characteristic read unsuccessful, status: $status")
            // Trying to read from the Time Characteristic? It doesnt have the property or permissions
            // set to allow this. Normally this would be an error and you would want to:
            // disconnectGattServer();
        }
    }


    private fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        val messageBytes = characteristic.value
        mClientActionListener.log("Read: " + ByteUtils.byteArrayInHexFormat(messageBytes))
        val message: String = ByteUtils.stringFromBytes(messageBytes)
        if (message == null) {
            mClientActionListener.logError("Unable to convert bytes to string")
            return
        }
        mClientActionListener.log("Received message: $message")
    }

}