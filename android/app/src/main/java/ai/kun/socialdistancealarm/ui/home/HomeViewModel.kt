package ai.kun.socialdistancealarm.ui.home

import ai.kun.opentracesdk_fat.BLETrace
import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

/**
 * The view model in our room with a view for the currently detected devices.
 *
 * @constructor
 * TODO
 *
 * @param application
 */
class HomeViewModel(application: Application) : AndroidViewModel(application) {

    val devices: MutableLiveData<List<Device>> = DeviceRepository.currentDevices
    val isStarted: MutableLiveData<Boolean> = BLETrace.isStarted
}