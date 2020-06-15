package ai.kun.socialdistancealarm.ui.history

import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val devices: LiveData<List<Device>>

    init {
        devices = DeviceRepository.allDevices
    }
}