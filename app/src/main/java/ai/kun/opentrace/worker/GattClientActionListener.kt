package ai.kun.opentrace.worker

interface GattClientActionListener {
    fun log(message: String)
    fun logError(message: String)
    fun setConnected(connected: Boolean)
    fun disconnectGattServer()
}