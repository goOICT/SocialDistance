package ai.kun.opentrace.ui.home

import ai.kun.opentrace.R
import ai.kun.opentrace.worker.BLETrace
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"

    private lateinit var homeViewModel: HomeViewModel

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_FINE_LOCATION = 2
    private val REQUEST_BACKGROUND_LOCATION = 3

    private var mIsForeground = true
    private var mIsChecking = false

    private var mIsTraceEnabled = true
    private var mFineLocationGranted = true
    private var mBackgroundLocationGranted = true
    private var mBluetoothEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Move this to a shared pref
        mIsTraceEnabled = true

        mFineLocationGranted = true
        mBackgroundLocationGranted = true
        mBluetoothEnabled = true
    }

    override fun onResume() {
        super.onResume()
        //TODO: Change this to enable with a shared pref
        context?.let {
            checkPermissions(it)
            checkBluetooth(it)
        }
        mIsForeground = true
        startTrace()
    }

    override fun onPause() {
        super.onPause()
        mIsForeground = false
        startTrace()
    }

    private fun startTrace() {
        if (mIsTraceEnabled && !mIsChecking && mBluetoothEnabled && mFineLocationGranted) {
            if (mIsForeground) {
                    BLETrace.stopBackground()
                    BLETrace.startForeground()
            } else {
                    BLETrace.stopForeground()
                    BLETrace.startBackground()
            }
        }
    }

    public fun checkPermissions(context: Context): Boolean {
        // Check permissions
        if (!mIsChecking && mIsTraceEnabled) {
            if (mFineLocationGranted &&
                context.applicationContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mIsChecking = true
                mFineLocationGranted = false
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_FINE_LOCATION
                )
                Log.d(TAG, "Requested user enable Location.")
                return false
            }
            if (mBackgroundLocationGranted &&
                context.applicationContext.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                mIsChecking = true
                mBackgroundLocationGranted = false
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    REQUEST_BACKGROUND_LOCATION
                )
                Log.d(TAG, "Requested user enable Location.")
                return false
            }

            return true
        } else {
            return false
        }
    }

    public fun checkBluetooth(context: Context): Boolean {
        // ??? lazy load this on a thread ???
        val bluetoothManager =
            context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        // Check low energy support
        if (!context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Get a newer device
            Log.e(TAG, "No LE Support.")
            mBluetoothEnabled = false
            return false
        }

        // Check advertising
        if (!bluetoothManager.adapter.isMultipleAdvertisementSupported) {
            // Unable to run the server on this device, get a better device
            Log.e(TAG, "No Advertising Support.")
            mBluetoothEnabled = false
            return false
        }

        // Check if bluetooth is enabled
        if (!bluetoothManager.adapter.isEnabled()) {
            // Request user to enable it
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            /* TODO: I need to add something to listen and see if Bluetooth is enabled...
            private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                 BluetoothAdapter.ERROR);
            switch (state) {
            case BluetoothAdapter.STATE_OFF:
                setButtonText("Bluetooth off");
                break;
            case BluetoothAdapter.STATE_TURNING_OFF:
                setButtonText("Turning Bluetooth off...");
                break;
            case BluetoothAdapter.STATE_ON:
                setButtonText("Bluetooth on");
                break;
            case BluetoothAdapter.STATE_TURNING_ON:
                setButtonText("Turning Bluetooth on...");
                break;
            }
        }
    }
};
             */
            mBluetoothEnabled = false
            return false
        }

        mBluetoothEnabled = true
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_BACKGROUND_LOCATION -> {
                if (!grantResults.isEmpty()) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "...Request for background location was denied.")
                        mIsChecking = false
                        mBackgroundLocationGranted = false
                        //TODO: add some code to disable background tracking in a shared pref
                    } else {
                        Log.w(TAG, "...Request for background location was granted.")
                        mIsChecking = false
                        mBackgroundLocationGranted = true
                    }
                } else {
                    // for some lame reason Android gives you a result with nothing in it before
                    // there is a real result?
                    Log.d(TAG, "Prompting for background location response...")
                }
            }
            REQUEST_FINE_LOCATION -> {
                if (!grantResults.isEmpty()) {
                    if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                        Log.w(TAG, "...Request for fine location was denied.")
                        mIsChecking = false
                        mFineLocationGranted = false
                        mIsTraceEnabled = false
                        //TODO: add some code to disable tracking in a shared pref and an indicator
                    } else {
                        Log.w(TAG, "...Request for fine location was granted.")
                        mFineLocationGranted = true
                        mIsChecking = false
                    }
                } else {
                    // for some lame reason Android gives you a result with nothing in it before
                    // there is a real result?
                    Log.d(TAG, "Prompting for fine location response...")
                }
            }
        }
    }

}
