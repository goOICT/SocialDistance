package ai.kun.socialdistancealarm.ui.home

import ai.kun.opentracesdk_fat.BLETrace
import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val devices: MutableLiveData<List<Device>> = DeviceRepository.currentDevices
    val isStarted: MutableLiveData<Boolean> = BLETrace.isStarted
}