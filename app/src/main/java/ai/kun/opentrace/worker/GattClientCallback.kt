package ai.kun.opentrace.worker

import android.bluetooth.*
import ai.kun.opentrace.util.BluetoothUtils
import ai.kun.opentrace.util.ByteUtils

class GattClientCallback(private val mClientActionListener: GattClientActionListener) :
    BluetoothGattCallback() {
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
            mClientActionListener.disconnectGattServer()
            return
        }
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            mClientActionListener.log("Connected to device " + gatt.device.address)
            mClientActionListener.setConnected(true)
            gatt.discoverServices()
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
            mClientActionListener.log("Disconnected from device")
            mClientActionListener.disconnectGattServer()
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        super.onServicesDiscovered(gatt, status)
        if (status != BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Device service discovery unsuccessful, status $status")
            return
        }
        val matchingCharacteristics: List<BluetoothGattCharacteristic> =
            BluetoothUtils.findCharacteristics(gatt)
        if (matchingCharacteristics.isEmpty()) {
            mClientActionListener.logError("Unable to find characteristics.")
            return
        }
        mClientActionListener.log("Initializing: setting write type and enabling notification")
        for (characteristic in matchingCharacteristics) {
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            enableCharacteristicNotification(gatt, characteristic)
        }
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int
    ) {
        super.onCharacteristicWrite(gatt, characteristic, status)
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log("Characteristic written successfully")
        } else {
            mClientActionListener.logError("Characteristic write unsuccessful, status: $status")
            mClientActionListener.disconnectGattServer()
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

    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        super.onCharacteristicChanged(gatt, characteristic)
        mClientActionListener.log("Characteristic changed, " + characteristic.uuid.toString())
        readCharacteristic(characteristic)
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int
    ) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mClientActionListener.log(
                "Descriptor written successfully: " + descriptor.uuid.toString()
            )
            mClientActionListener.initializeTime()
        } else {
            mClientActionListener.logError(
                "Descriptor write unsuccessful: " + descriptor.uuid.toString()
            )
        }
    }

    private fun enableCharacteristicNotification(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val characteristicWriteSuccess =
            gatt.setCharacteristicNotification(characteristic, true)
        if (characteristicWriteSuccess) {
            mClientActionListener.log(
                "Characteristic notification set successfully for " + characteristic.uuid
                    .toString()
            )
            if (BluetoothUtils.isEchoCharacteristic(characteristic)) {
                mClientActionListener.initializeEcho()
            } else if (BluetoothUtils.isTimeCharacteristic(characteristic)) {
                enableCharacteristicConfigurationDescriptor(gatt, characteristic)
            }
        } else {
            mClientActionListener.logError(
                "Characteristic notification set failure for " + characteristic.uuid.toString()
            )
        }
    }

    // Sometimes the Characteristic does not have permissions, and instead its Descriptor holds them
    // See https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
    private fun enableCharacteristicConfigurationDescriptor(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic
    ) {
        val descriptorList =
            characteristic.descriptors
        val descriptor: BluetoothGattDescriptor? =
            BluetoothUtils.findClientConfigurationDescriptor(descriptorList)
        if (descriptor == null) {
            mClientActionListener.logError("Unable to find Characteristic Configuration Descriptor")
            return
        }
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        val descriptorWriteInitiated = gatt.writeDescriptor(descriptor)
        if (descriptorWriteInitiated) {
            mClientActionListener.log(
                "Characteristic Configuration Descriptor write initiated: " + descriptor.uuid
                    .toString()
            )
        } else {
            mClientActionListener.logError(
                "Characteristic Configuration Descriptor write failed to initiate: " + descriptor.uuid
                    .toString()
            )
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