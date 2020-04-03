package ai.kun.opentrace.worker

import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils
import ai.kun.opentrace.util.Constants.CHARACTERISTIC_ECHO_UUID
import ai.kun.opentrace.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_UUID
import android.bluetooth.*

class GattServerCallback(private val mServerActionListener: GattServerActionListener) :
    BluetoothGattServerCallback() {
    override fun onConnectionStateChange(
        device: BluetoothDevice,
        status: Int,
        newState: Int
    ) {
        super.onConnectionStateChange(device, status, newState)
        mServerActionListener.log(
            """
                onConnectionStateChange ${device.address}
                status $status
                newState $newState
                """.trimIndent()
        )
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mServerActionListener.addDevice(device)
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mServerActionListener.removeDevice(device)
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
        mServerActionListener.log(
            "onCharacteristicReadRequest " + characteristic.uuid.toString()
        )
        if (BluetoothUtils.requiresResponse(characteristic)) {
            // Unknown read characteristic requiring response, send failure
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_FAILURE,
                0,
                ByteArray(0)
            )
        }
        // Not one of our characteristics or has NO_RESPONSE property set
    }

    // The Gatt will reject Characteristic Write requests that do not have the permission set,
    // so there is no need to check inside the callback
    override fun onCharacteristicWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        characteristic: BluetoothGattCharacteristic,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        super.onCharacteristicWriteRequest(
            device,
            requestId,
            characteristic,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        mServerActionListener.log(
            """
                onCharacteristicWriteRequest${characteristic.uuid}
                Received: ${ByteUtils.byteArrayInHexFormat(value)}
                """.trimIndent()
        )
        if (CHARACTERISTIC_ECHO_UUID.equals(characteristic.uuid)) {
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                ByteArray(0)
            )

            // Reverse message to differentiate original message & response
            val response: ByteArray = ByteUtils.reverse(value)
            characteristic.value = response
            mServerActionListener.log("Sending: " + ByteUtils.byteArrayInHexFormat(response))
            mServerActionListener.notifyCharacteristicEcho(response)
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
        mServerActionListener.log("onDescriptorReadRequest" + descriptor.uuid.toString())
    }

    // The Gatt will reject Descriptor Write requests that do not have the permission set,
    // so there is no need to check inside the callback
    override fun onDescriptorWriteRequest(
        device: BluetoothDevice,
        requestId: Int,
        descriptor: BluetoothGattDescriptor,
        preparedWrite: Boolean,
        responseNeeded: Boolean,
        offset: Int,
        value: ByteArray
    ) {
        super.onDescriptorWriteRequest(
            device,
            requestId,
            descriptor,
            preparedWrite,
            responseNeeded,
            offset,
            value
        )
        mServerActionListener.log(
            """
                onDescriptorWriteRequest: ${descriptor.uuid}
                value: ${ByteUtils.byteArrayInHexFormat(value)}
                """.trimIndent()
        )
        if (CLIENT_CONFIGURATION_DESCRIPTOR_UUID.equals(descriptor.uuid)) {
            mServerActionListener.addClientConfiguration(device, value)
            mServerActionListener.sendResponse(
                device,
                requestId,
                BluetoothGatt.GATT_SUCCESS,
                0,
                ByteArray(0)
            )
        }
    }

    override fun onNotificationSent(device: BluetoothDevice, status: Int) {
        super.onNotificationSent(device, status)
        mServerActionListener.log("onNotificationSent")
    }

}