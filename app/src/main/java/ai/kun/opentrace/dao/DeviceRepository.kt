package ai.kun.opentrace.dao

import ai.kun.opentrace.ui.api.FirebaseOpenTraceApi
import android.bluetooth.le.ScanResult
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData

class DeviceRepository(private val deviceDao: DeviceDao) {

    val allDevices: LiveData<List<Device>> = deviceDao.getDevicesSeen()

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(device: Device) {
        FirebaseOpenTraceApi().submitTrace(device.deviceUuid, device.distance, device.rssi,
            device.txPower, device.timeStampNanos, device.sessionId)
        deviceDao.insert(device)
    }
}