package ai.kun.opentrace.ui.home

import ai.kun.opentrace.R
import ai.kun.opentrace.worker.BLEClient
import ai.kun.opentrace.worker.BLEServer
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
    val mBleServer : BLEServer = BLEServer()
    val mBleClient : BLEClient = BLEClient()

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

        val bluetoothManager = context!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        // Check if bluetooth is enabled
        if (!bluetoothAdapter.isEnabled()) {
            // Request user to enable it
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivity(enableBtIntent)
            return
        }

        // Check low energy support
        if (!activity!!.packageManager!!.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // Get a newer device
            Log.e(TAG, "No LE Support.")
            return
        }

        // Check advertising
        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
            // Unable to run the server on this device, get a better device
            Log.e(TAG, "No Advertising Support.")
            return
        }

        mBleServer.enable(context!!)
        mBleClient.enable(activity!!)
    }
}
