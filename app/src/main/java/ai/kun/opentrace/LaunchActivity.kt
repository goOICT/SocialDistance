package ai.kun.opentrace

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth


/*
    At app startup we need to decide if we should redirect user to:
        - Sign-In screen (if he was not logged int)
        - Home screen (if already logged in)
    This takes place in Launch Activity.
 */
class LaunchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        val model: LaunchViewModel by viewModels()
        when (model.isLoggedIn) {
            true -> moveToHome()
            false -> moveToSignIn()
        }
    }

    private fun moveToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private fun moveToSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            RC_SIGN_IN
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("LaunchActivity", "onActivityResult  $requestCode $resultCode")
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1004
    }
}