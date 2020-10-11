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


/**
 * This doesn't really do much of anything.  When we started the project we thought there might be
 * some kind of login for corp. accounts, so if that's what you're building, you want to put that
 * stuff here.  Originally the plan was to use firebase for that.  Right now all it really does
 * is decide if you should see the home fragment next or the onboarding fragment.
 *
 */
class LaunchFragment : Fragment() {
    private val TAG = "LaunchFragment"

    /**
     * hide the options menu since this is a splash screen
     *
     * @param savedInstanceState
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    /**
     * Inflate and return
     *
     * @param inflater the layout inflater
     * @param container the container
     * @param savedInstanceState not used
     * @return the view
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_launch, container, false)
        return root
    }

    /**
     * We used to wait for the firebase login here, but we don't anymore.
     *
     * @param view the view
     * @param savedInstanceState not used
     */
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

    /**
     * This is where we decide if we are actually going to launch the home fragment or if we
     * are going to launch the onboard fragment.
     *
     */
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

    /**
     * Firebase login is disabled.  We just call move to home.
     *
     */
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