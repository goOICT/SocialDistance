package ai.kun.opentracesdk_fat.util

import ai.kun.opentracesdk_fat.util.Constants.ANDROID_SERVICE_STRING
import ai.kun.opentracesdk_fat.util.Constants.CHARACTERISTIC_DEVICE_STRING
import ai.kun.opentracesdk_fat.util.Constants.CHARACTERISTIC_USER_STRING
import ai.kun.opentracesdk_fat.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

object BluetoothUtils {

    fun findCharacteristic(
        bluetoothGatt: BluetoothGatt,
        uuidString: String
    ): BluetoothGattCharacteristic? {
        val serviceList = bluetoothGatt.services
        val service = findService(serviceList) ?: return null
        val characteristicList =
            service.characteristics
        for (characteristic in characteristicList) {
            if (characteristicMatches(characteristic, uuidString)) {
                return characteristic
            }
        }
        return null
    }

    fun isDeviceCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_DEVICE_STRING)
    }

    fun isUserCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_USER_STRING)
    }

    private fun characteristicMatches(
        characteristic: BluetoothGattCharacteristic?,
        uuidString: String
    ): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return uuidMatches(uuid.toString(), uuidString)
    }

    private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return matchesCharacteristicUuidString(uuid.toString())
    }

    private fun matchesCharacteristicUuidString(characteristicIdString: String): Boolean {
        return uuidMatches(
            characteristicIdString,
            CHARACTERISTIC_DEVICE_STRING,
            CHARACTERISTIC_USER_STRING
        )
    }

    fun requiresResponse(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
    }

    fun requiresConfirmation(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE
                == BluetoothGattCharacteristic.PROPERTY_INDICATE)
    }

    // Descriptor
    fun findClientConfigurationDescriptor(descriptorList: List<BluetoothGattDescriptor>): BluetoothGattDescriptor? {
        for (descriptor in descriptorList) {
            if (isClientConfigurationDescriptor(descriptor)) {
                return descriptor
            }
        }
        return null
    }

    private fun isClientConfigurationDescriptor(descriptor: BluetoothGattDescriptor?): Boolean {
        if (descriptor == null) {
            return false
        }
        val uuid = descriptor.uuid
        val uuidSubstring = uuid.toString().substring(4, 8)
        return uuidMatches(uuidSubstring, CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID)
    }

    /**
     * Calculates the strength of the signal from another handset taking into consideration
     * the type of handset and the reported transmission power.
     *
     * @param rssi The RSSI reported from the scan
     * @param txPower The transmission power reported from the scan
     * @param isAndroid If the handset was iOS or Android
     * @return A signal strength
     */
    fun calculateSignal(rssi: Int, txPower: Int, isAndroid: Boolean): Int {
        // Fix for older handset that don't report power...
        val adjustedTxPower = if (txPower + rssi < 0) Constants.ASSUMED_TX_POWER else txPower

        // Notify the user when we are adding a device that's too close
        var signal = adjustedTxPower + rssi

        if (!isAndroid) signal -= Constants.IOS_SIGNAL_REDUCTION

        return signal
    }



    // Service
    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, ANDROID_SERVICE_STRING)
    }

    fun findService(serviceList: List<BluetoothGattService>): BluetoothGattService? {
        for (service in serviceList) {
            val serviceIdString = service.uuid
                .toString()
            if (matchesServiceUuidString(serviceIdString)) {
                return service
            }
        }
        return null
    }

    // String matching
    // If manually filtering, substring to match:
    // 0000XXXX-0000-0000-0000-000000000000
    private fun uuidMatches(
        uuidString: String,
        vararg matches: String
    ): Boolean {
        for (match in matches) {
            if (uuidString.equals(match, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
}