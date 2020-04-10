package ai.kun.opentrace.ui.launch

import ai.kun.opentrace.worker.BLETrace
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class LaunchViewModel: ViewModel(), FirebaseAuth.AuthStateListener {
    private val authState = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = authState

    init {
        FirebaseAuth.getInstance().addAuthStateListener(this)
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseAuth.getInstance().removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(auth: FirebaseAuth) {
        authState.value = auth.currentUser != null

        // For now we are using the firebase user's ID as the device id
        // we could have used something actually tied to the physical device
        // or something input by the user too, but doing this allows a user that
        // logs in to move from one device to another.  Note that we could still
        // loose track of the user if they never log in and then switch devices.
        // If they login later we'll still be good though.  Firebase takes care
        // of that.
        BLETrace.uniqueId = auth.currentUser?.uid
    }
}