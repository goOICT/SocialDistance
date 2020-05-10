package ai.kun.socialdistancealarm.ui.home

import ai.kun.socialdistancealarm.alarm.BLETrace
import ai.kun.socialdistancealarm.dao.Device
import ai.kun.socialdistancealarm.dao.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val devices: MutableLiveData<List<Device>> = DeviceRepository.currentDevices
    val isStarted: MutableLiveData<Boolean> = BLETrace.isStarted
}