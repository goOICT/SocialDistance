package ai.kun.opentracesdk_fat


import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.dao.DeviceDao
import ai.kun.opentracesdk_fat.dao.DeviceRoomDatabase
import ai.kun.opentracesdk_fat.util.BluetoothUtils
import ai.kun.opentracesdk_fat.util.Constants
import ai.kun.opentracesdk_fat.util.NotificationUtils
import android.content.Context
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.GlobalScope

object DeviceRepository {

    private lateinit var deviceDao: DeviceDao
    val currentDevices: MutableLiveData<List<Device>> = MutableLiveData<List<Device>>()

    lateinit var allDevices: LiveData<List<Device>>

    fun init(applicationContext: Context) {
        deviceDao = DeviceRoomDatabase.getDatabase(
            applicationContext,
            GlobalScope
        ).deviceDao()
        allDevices = deviceDao.getAllDevices()
    }

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(device: Device) {
        deviceDao.insert(device)
        currentDevices.postValue(
            getCurrentDevices()
        )

        // Alert if we need to...
        if (!device.isTeamMember) {
            val signal = BluetoothUtils.calculateSignal(device.rssi, device.txPower, device.isAndroid)

            when {
                signal <= Constants.SIGNAL_DISTANCE_OK -> {
                }
                signal <= Constants.SIGNAL_DISTANCE_LIGHT_WARN -> {
                }
                signal <= Constants.SIGNAL_DISTANCE_STRONG_WARN -> {
                    NotificationUtils.sendNotificationDanger()
                }
                else -> {
                    NotificationUtils.sendNotificationTooClose()
                }
            }
        }
        //TODO: delete anything that's too old
    }

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateCurrentDevices() {
        currentDevices.postValue(
            getCurrentDevices()
        )
    }

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun noCurrentDevices() {
        currentDevices.postValue(emptyList())
    }


    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun deleteAll() {
        deviceDao.deleteAllDevices()
    }

    private fun getCurrentDevices(): List<Device> {
        // Get a rolling average of the devices seen over the last two scans
        val devices = deviceDao.getCurrentDevicesOrderByRssi(
            System.currentTimeMillis() - (Constants.FOREGROUND_TRACE_INTERVAL * 2),
            System.currentTimeMillis())
        var averagedDevices  = mutableListOf<Device>()
        for (device in devices) {
            val current = averagedDevices.lastOrNull { it.deviceUuid.contentEquals(device.deviceUuid) }
            current?.let {
                it.rssi = (it.rssi + device.rssi).div(2)
                it.txPower = (it.txPower + device.txPower).div(2)
            } ?: averagedDevices.add(device)
        }

        return averagedDevices
    }
}