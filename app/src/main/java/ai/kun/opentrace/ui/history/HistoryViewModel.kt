package ai.kun.opentrace.ui.history

import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.dao.DeviceRepository
import ai.kun.opentrace.dao.DeviceRoomDatabase
import android.app.Application
import androidx.lifecycle.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val deviceRepository: DeviceRepository
    val devices: LiveData<List<Device>>

    init {
        val devicesDao = DeviceRoomDatabase.getDatabase(application, viewModelScope).deviceDao()
        deviceRepository = DeviceRepository(devicesDao)
        devices = deviceRepository.allDevices
    }
}