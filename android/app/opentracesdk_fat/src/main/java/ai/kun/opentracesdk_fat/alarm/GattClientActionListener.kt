package ai.kun.opentracesdk_fat.alarm

interface GattClientActionListener {
    fun log(message: String)
    fun logError(message: String)
    fun setConnected(connected: Boolean)
    fun disconnectGattServer()
}