package ai.kun.opentracesdk_fat.alarm

/**
 * We probably don't need this at the moment, but I've kept it in case we want to support connections.
 * That was the original intent of the interface.
 *
 */
interface GattClientActionListener {
    fun log(message: String)
    fun logError(message: String)
    fun setConnected(connected: Boolean)
    fun disconnectGattServer()
}