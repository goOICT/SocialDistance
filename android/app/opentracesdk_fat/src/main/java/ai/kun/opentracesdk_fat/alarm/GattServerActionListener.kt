package ai.kun.opentracesdk_fat.alarm

import android.bluetooth.BluetoothDevice


/**
 * This class could probably be removed.  It was originally intended to be an interface that supports
 * connections between devices, but right now the devices don't connect.
 *
 */
interface GattServerActionListener {
    fun log(message: String)

    fun addDevice(device: BluetoothDevice)

    fun removeDevice(device: BluetoothDevice)

    fun addClientConfiguration(device: BluetoothDevice, value: ByteArray)

    fun sendResponse(
        device: BluetoothDevice,
        requestId: Int,
        status: Int,
        offset: Int,
        value: ByteArray
    )
}