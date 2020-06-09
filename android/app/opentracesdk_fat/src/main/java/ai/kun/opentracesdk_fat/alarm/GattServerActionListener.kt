package ai.kun.opentracesdk_fat.alarm

import android.bluetooth.BluetoothDevice


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