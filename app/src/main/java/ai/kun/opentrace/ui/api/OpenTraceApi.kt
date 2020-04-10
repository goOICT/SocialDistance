package ai.kun.opentrace.ui.api

import androidx.lifecycle.LiveData

interface OpenTraceApi {
    fun trackVisit(otherIdentity: String): LiveData<Boolean>
    fun submitSymptoms(symptoms: Set<String>): ResultLiveData<Int>
    fun setDeviceUuid(deviceUuid: String)
    fun submitTrace(deviceUuid: String,  distance: Float?, rssi: Int, txPower: Int, timeStampNanos: Long, sessionId: String): ResultLiveData<Int>
}