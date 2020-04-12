package ai.kun.opentrace.ui.launch

import ai.kun.opentrace.MainActivity
import ai.kun.opentrace.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse


/*
    At app startup we need to decide if we should redirect user to:
        - Sign-In screen (if he was not logged int)
        - Home screen (if already logged in)
    This takes place in Launch Fragment.
 */
class LaunchFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_launch, container, false)

        val model: LaunchViewModel by viewModels()
        val authStateObserver = Observer<Boolean> {isLoggedIn ->
            when (isLoggedIn) {
                true -> findNavController().navigate(R.id.action_launchFragment_to_navigation_home)
                false -> moveToSignIn()
            }
        }
        model.isLoggedIn.observe(viewLifecycleOwner, authStateObserver)

        return root
    }

    private fun moveToHome() {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
    }

    private fun moveToSignIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.AnonymousBuilder().build(),
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
            if (resultCode == Activity.RESULT_OK) {
                moveToHome()
            } else {
                response?.error?.let {
                    Toast.makeText(context, it.message, Toast.LENGTH_LONG).show()
                }

            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 1004
    }
}