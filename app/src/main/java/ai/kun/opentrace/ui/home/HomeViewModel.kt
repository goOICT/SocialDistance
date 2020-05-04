package ai.kun.opentrace.ui.home

import ai.kun.opentrace.alarm.BLEClient
import ai.kun.opentrace.alarm.BLETrace
import ai.kun.opentrace.dao.Device
import ai.kun.opentrace.dao.DeviceRepository
import android.app.Application
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val devices: MutableLiveData<List<Device>> = DeviceRepository.currentDevices
    val isStarted: MutableLiveData<Boolean> = BLETrace.isStarted
}