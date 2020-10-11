package ai.kun.socialdistancealarm.ui.history

import ai.kun.opentracesdk_fat.dao.Device
import ai.kun.opentracesdk_fat.DeviceRepository
import android.app.Application
import androidx.lifecycle.*

/**
 * History view model using live data from the device repository.  We use a database to store
 * detections of other devices.
 *
 * @constructor
 * Build new live data against the database to show contacts in the history fragment
 *
 * @param application
 */
class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    val devices: LiveData<List<Device>>

    init {
        devices = DeviceRepository.allDevices
    }
}