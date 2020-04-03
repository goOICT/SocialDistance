package ai.kun.opentrace.ui.api

import androidx.lifecycle.LiveData

interface OpenTraceApi {
    fun trackVisit(firstId: String, secondId: String): LiveData<Boolean>
}