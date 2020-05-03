package ai.kun.opentrace.alarm

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.util.Log

object BtleScanCallback: ScanCallback() {
    private val TAG = "BtleScanCallback"
    val mScanResults = HashMap<String, ScanResult>()
    val handler = Handler()

    override fun onScanResult(
        callbackType: Int,
        result: ScanResult
    ) {
        addScanResult(result)
    }

    override fun onBatchScanResults(results: List<ScanResult>) {
        for (result in results) {
            addScanResult(result)
        }
    }

    override fun onScanFailed(errorCode: Int) {
        Log.e(TAG, "BLE Scan Failed with code $errorCode")
    }

    private fun addScanResult(result: ScanResult) {
        synchronized(this) {
            val deviceAddress = result.device.address
            mScanResults[deviceAddress] = result
        }
    }
}