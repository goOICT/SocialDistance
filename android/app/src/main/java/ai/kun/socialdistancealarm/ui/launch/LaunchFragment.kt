package ai.kun.socialdistancealarm.ui.launch

import ai.kun.socialdistancealarm.R
import ai.kun.socialdistancealarm.util.Constants
import ai.kun.opentracesdk_fat.BLETrace
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController


/*
 *  At app startup we need to sign in anonymously so we can use Firebase
 *  This takes place in Launch Fragment.
 */
class LaunchFragment : Fragment() {
    private val TAG = "LaunchFragment"

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
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val model: LaunchViewModel by viewModels()
        val authStateObserver = Observer<Boolean> {isLoggedIn ->
            when (isLoggedIn) {
                true -> moveToHome()
                false -> waitForSignIn()
            }
        }
        model.isLoggedIn.observe(viewLifecycleOwner, authStateObserver)
    }

    private fun moveToHome() {
        val sharedPrefs = context?.applicationContext?.getSharedPreferences(
            Constants.PREF_FILE_NAME, Context.MODE_PRIVATE
        )

        if (sharedPrefs == null || !sharedPrefs.getBoolean(Constants.PREF_IS_ONBOARDED, false)) {
            BLETrace.uuidString = BLETrace.getNewUniqueId()
            BLETrace.init(requireContext().applicationContext)
            findNavController().navigate(R.id.action_launchFragment_to_onBoardFragment_1)
        } else {
            findNavController().navigate(R.id.action_launchFragment_to_navigation_home)
        }
    }

    private fun waitForSignIn() {
/*        val auth = FirebaseAuth.getInstance()
        val task = auth.signInAnonymously()
        task.addOnCompleteListener() {
            if(it.isSuccessful) {
                Log.d(TAG, "signInAnonymously:success")
            }
            else {
                Log.w(TAG, "signInAnonymously:failure", task.exception)
                Toast.makeText(context, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            }
        }*/

        moveToHome()
    }
}