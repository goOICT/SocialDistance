package ai.kun.socialdistancealarm.dao

import ai.kun.socialdistancealarm.util.Constants
import ai.kun.socialdistancealarm.util.NotificationUtils
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
        deviceDao = DeviceRoomDatabase.getDatabase(applicationContext, GlobalScope).deviceDao()
        allDevices = deviceDao.getAllDevices()
    }

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(device: Device) {
        deviceDao.insert(device)
        currentDevices.postValue(getCurrentDevices())
        // Notify the user when we are adding a device that's too close
        val signal = device.txPower + device.rssi
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
        //TODO: delete anything that's too old
    }

    // thread, blocking the UI.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateCurrentDevices() {
        currentDevices.postValue(getCurrentDevices())
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
        return deviceDao.getCurrentDevicesOrderByRssi(
            System.currentTimeMillis() - (Constants.FOREGROUND_TRACE_INTERVAL),
            System.currentTimeMillis())
    }
}