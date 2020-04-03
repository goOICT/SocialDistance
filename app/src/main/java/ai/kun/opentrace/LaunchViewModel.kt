package ai.kun.opentrace

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth

class LaunchViewModel: ViewModel() {
    val isLoggedIn: Boolean
        get() {
            val auth = FirebaseAuth.getInstance()
            return auth.currentUser != null
        }

    init {
        Log.d("LaunchViewModel", "init!")
    }
}