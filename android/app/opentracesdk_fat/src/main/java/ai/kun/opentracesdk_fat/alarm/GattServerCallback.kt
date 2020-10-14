package ai.kun.opentracesdk_fat.alarm

import ai.kun.opentracesdk_fat.util.ByteUtils
import ai.kun.opentracesdk_fat.util.Constants
import android.bluetooth.*

/**
 * Implement connection to the GATT server.  Right now this code isn't really called, but we built
 * the library to support connection between BLE devices thinking that we might need it.
 */
object GattServerCallback: BluetoothGattServerCallback() {

    var serverActionListener: GattServerActionListener? = null

    override fun onConnectionStateChange(
        device: BluetoothDevice,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(device, status, newState)
        serverActionListener?.log(
            """
                onConnectionStateChange ${device.address}
                status $status
                newState $newState
                """.trimIndent()
        )
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            serverActionListener?.addDevice(device)
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            serverActionListener?.removeDevice(device)
        }
    }

    // The Gatt will reject Characteristic Read requests that do not have the permission set,
    // so there is no need to check inside the callback
    override fun onCharacteristicReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
        serverActionListener?.log(
            "onCharacteristicReadRequest " + characteristic.uuid.toString()
        )
        when {
            Constants.CHARACTERISTIC_DEVICE_UUID == characteristic.uuid -> {
                serverActionListener?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    ByteUtils.bytesFromString("SomeDeviceID")
                )
            }
            Constants.CHARACTERISTIC_USER_UUID == characteristic.uuid -> {
                serverActionListener?.sendResponse(
                    device,
                    requestId,
                    BluetoothGatt.GATT_FAILURE,
                    0,
                    ByteUtils.bytesFromString("SomeUserID")
                )
            }
        }

    }

    // The Gatt will reject Descriptor Read requests that do not have the permission set,
    // so there is no need to check inside the callback
    override fun onDescriptorReadRequest(
        device: BluetoothDevice,
        requestId: Int,
        offset: Int,
        descriptor: BluetoothGattDescriptor
    ) {
        super.onDescriptorReadRequest(device, requestId, offset, descriptor)
        serverActionListener?.log("onDescriptorReadRequest" + descriptor.uuid.toString())
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        super.onNotificationSent(device, status)
        serverActionListener?.log("onNotificationSent")
    }

}