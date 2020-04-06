package ai.kun.opentrace.ui.api

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class FirebaseOpenTraceApi: OpenTraceApi {
    companion object {
        const val FUNCTION_TRACK_VISIT = "visitIdentity"
    }

    private var functions = Firebase.functions

    override fun trackVisit(firstId: String, secondId: String): LiveData<Boolean> {
        val data = hashMapOf(
            "firstIdentity" to firstId,
            "secondIdentity" to secondId
        )

        val liveData = MutableLiveData<Boolean>()

        functions.getHttpsCallable(FUNCTION_TRACK_VISIT)
            .call(data)
            .addOnCompleteListener { result ->
                liveData.value = result.isSuccessful
            }

        return liveData
    }
}