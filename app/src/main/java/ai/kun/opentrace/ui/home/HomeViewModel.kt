package ai.kun.opentrace.ui.home

import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.dao.DeviceRepository
import ai.kun.opentrace.dao.DeviceRoomDatabase
import android.app.Application
import androidx.lifecycle.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val devices: MutableLiveData<List<Device>>

    init {
        devices = DeviceRepository.currentDevices
    }
}