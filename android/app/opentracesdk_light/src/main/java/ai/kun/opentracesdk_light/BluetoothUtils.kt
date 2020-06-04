package ai.kun.opentracesdk_light

import android.app.AlarmManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast

class BluetoothUtils {
    companion object {
        fun deviceSupportsBle(context: Context): Boolean {
            val bluetoothManager =
                context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

            // Check if it's even there
            if (bluetoothManager.adapter == null) {
                return false
            }

            // Check low energy support
            if (!context.applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return false
            }

            // Check advertising
            if (!bluetoothManager.adapter.isMultipleAdvertisementSupported) {
                return false
            }

            return true
        }

        fun bleIsEnabled(context: Context): Boolean {
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
                    ?: return false
            return bluetoothManager.adapter.isEnabled
        }

        fun getBluetoothManager(context: Context): BluetoothManager {
            return context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        }

        fun getBluetoothScanner(context: Context): BluetoothLeScanner {
            return getBluetoothManager(context).adapter.bluetoothLeScanner
        }

        fun getBluetoothAdvertiser(context: Context): BluetoothLeAdvertiser {
            return getBluetoothManager(context).adapter.bluetoothLeAdvertiser
        }

        fun getAlarmManager(context: Context): AlarmManager {
            return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        }
    }
}