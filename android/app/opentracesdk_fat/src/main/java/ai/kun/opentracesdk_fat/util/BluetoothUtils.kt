package ai.kun.opentracesdk_fat.util

import ai.kun.opentracesdk_fat.util.Constants.ANDROID_SERVICE_STRING
import ai.kun.opentracesdk_fat.util.Constants.CHARACTERISTIC_DEVICE_STRING
import ai.kun.opentracesdk_fat.util.Constants.CHARACTERISTIC_USER_STRING
import ai.kun.opentracesdk_fat.util.Constants.CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService

/**
 * Some utilities to help us with various Bluetooth things.
 */
object BluetoothUtils {

    /**
     * find a characteristic inside the GATT
     *
     * @param bluetoothGatt The GATT to search
     * @param uuidString The UUID of the characteristic we want to find
     * @return The characteristic
     */
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

    /**
     * Right now we don't connect so we don't use this, but this method checks to see if this
     * is our device characteristic
     *
     * @param characteristic The characteristic to check
     * @return True if this matches the one we have in the constants
     */
    fun isDeviceCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_DEVICE_STRING)
    }
    /**
     * Right now we don't connect so we don't use this, but this method checks to see if this
     * is our user characteristic
     *
     * @param characteristic The characteristic to check
     * @return True if this matches the one we have in the constants
     */
    fun isUserCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        return characteristicMatches(characteristic, CHARACTERISTIC_USER_STRING)
    }

    /**
     * A helper function for checking matches.
     */
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

    /**
     * Checks to see if the characteristic
     *
     * @param characteristic the characteristic to match
     * @return True if they match
     */
    private fun isMatchingCharacteristic(characteristic: BluetoothGattCharacteristic?): Boolean {
        if (characteristic == null) {
            return false
        }
        val uuid = characteristic.uuid
        return matchesCharacteristicUuidString(uuid.toString())
    }

    /**
     * Checks to see if the characteristic matches the UUID strings in the constants
     *
     * @param characteristic the characteristic to match
     * @return True if they match
     */
    private fun matchesCharacteristicUuidString(characteristicIdString: String): Boolean {
        return uuidMatches(
            characteristicIdString,
            CHARACTERISTIC_DEVICE_STRING,
            CHARACTERISTIC_USER_STRING
        )
    }

    /**
     * Checks to see if the characteristic needs a response
     *
     * @param characteristic the characteristic to check
     * @return True if they match
     */
    fun requiresResponse(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE
                != BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
    }

    /**
     * Checks to see if the characteristic needs a confirmation
     *
     * @param characteristic the characteristic to check
     * @return True if they match
     */
    fun requiresConfirmation(characteristic: BluetoothGattCharacteristic): Boolean {
        return (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE
                == BluetoothGattCharacteristic.PROPERTY_INDICATE)
    }

    /**
     * find the configuration descriptor
     *
     * @param descriptorList The list from the GATT
     * @return The descriptor
     */
    fun findClientConfigurationDescriptor(descriptorList: List<BluetoothGattDescriptor>): BluetoothGattDescriptor? {
        for (descriptor in descriptorList) {
            if (isClientConfigurationDescriptor(descriptor)) {
                return descriptor
            }
        }
        return null
    }

    /**
     * Check to see if the configuration descriptor matches the one in the constants
     *
     * @param descriptor the descriptor to check
     * @return True if it matches
     */
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


    /**
     * Check and see if this service string matches the one in the constants
     *
     * @param serviceIdString The service string to check
     * @return True if if matches the one in the constants
     */
    private fun matchesServiceUuidString(serviceIdString: String): Boolean {
        return uuidMatches(serviceIdString, ANDROID_SERVICE_STRING)
    }

    /**
     * find the service
     *
     * @param serviceList The service list from the GATT
     * @return The service that matches
     */
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

    /**
     * a simple matching function for UUID's
     *
     * @param uuidString The UUID String to match
     * @param matches The match
     * @return True if they match
     */
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