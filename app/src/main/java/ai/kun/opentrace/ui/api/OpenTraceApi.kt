package ai.kun.opentrace.ui.api

import androidx.lifecycle.LiveData

interface OpenTraceApi {
    fun trackVisit(otherIdentity: String): LiveData<Boolean>
    fun submitSymptoms(symptoms: Set<String>): ResultLiveData<Int>
}