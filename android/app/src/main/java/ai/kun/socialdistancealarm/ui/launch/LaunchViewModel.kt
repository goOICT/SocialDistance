package ai.kun.socialdistancealarm.ui.launch

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

/**
 * View model for authentication.  We no longer use this, but you may need it
 * if you build something that has sign-in.
 *
 */
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
    }
}