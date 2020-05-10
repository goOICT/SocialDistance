package ai.kun.socialdistancealarm.ui.history

import ai.kun.socialdistancealarm.dao.Device
import ai.kun.socialdistancealarm.dao.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val devices: LiveData<List<Device>>

    init {
        devices = DeviceRepository.allDevices
    }
}