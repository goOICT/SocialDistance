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

        // Alert if we need to...
        if (!device.isTeamMember) {
            // Fix for older handset that don't report power...
            val txPower = if (device.txPower + device.rssi < 0) 127 else device.txPower

            // Notify the user when we are adding a device that's too close
            val signal = txPower + device.rssi

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
            // TODO: AFC adjust the * 1.5
            System.currentTimeMillis() - (Constants.FOREGROUND_TRACE_INTERVAL + 500),
            System.currentTimeMillis())
    }
}