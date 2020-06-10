package ai.kun.opentracesdk_fat

import ai.kun.opentracesdk_fat.alarm.BLEClient
import ai.kun.opentracesdk_fat.alarm.BLEServer
import ai.kun.opentracesdk_fat.alarm.GattServerCallback
import ai.kun.opentracesdk_fat.util.Constants
import ai.kun.opentracesdk_fat.util.Constants.PREF_FILE_NAME
import ai.kun.opentracesdk_fat.util.Constants.PREF_IS_PAUSED
import ai.kun.opentracesdk_fat.util.Constants.PREF_TEAM_IDS
import ai.kun.opentracesdk_fat.util.Constants.PREF_UNIQUE_ID
import ai.kun.opentracesdk_fat.util.NotificationUtils
import android.app.AlarmManager
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeAdvertiser
import android.bluetooth.le.BluetoothLeScanner
import android.content.Context
import android.content.SharedPreferences
import android.location.LocationManager
import android.util.Log
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.MutableLiveData
import java.util.*
import kotlin.collections.HashSet


object BLETrace {
    private val mBleServer : BLEServer =
        BLEServer()
    private val mBleClient : BLEClient =
        BLEClient()
    private val TAG = "BLETrace"

    private var isInit = false
    var isReactNative = false
    private lateinit var context : Context
    var bluetoothGattServer : BluetoothGattServer? = null
    var bluetoothManager : BluetoothManager? = null
    var bluetoothLeScanner : BluetoothLeScanner? = null
    var bluetoothLeAdvertiser : BluetoothLeAdvertiser? = null

    var isBackground : Boolean = true
    val isStarted: MutableLiveData<Boolean> = MutableLiveData(false)
    var isPaused : Boolean
        get() {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                return sharedPrefs.getBoolean(PREF_IS_PAUSED, false)
            }
        }
        set(value) {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                editor.putBoolean(PREF_IS_PAUSED, value)
                editor.apply()
                if (value) {
                    stop()
                } else {
                    start(isBackground)
                }
            }
        }

    var uuidString : String?
        get() {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                return sharedPrefs.getString(PREF_UNIQUE_ID, null)
            }
        }
        set(value) {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                if (value != null) {
                    editor.putString(PREF_UNIQUE_ID, value)
                    editor.commit()
                    init(context, this.isReactNative)
                    deviceNameServiceUuid = UUID.fromString(value)
                } else {
                    editor.remove(PREF_UNIQUE_ID)
                    editor.commit()
                    stop()
                }
            }
        }

    var teamUuids : Set<String>?
        get() {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                return sharedPrefs.getStringSet(PREF_TEAM_IDS, HashSet<String>())
            }
        }
        set(value) {
            synchronized(this) {
                val sharedPrefs = context.getSharedPreferences(
                    PREF_FILE_NAME, Context.MODE_PRIVATE
                )
                val editor: SharedPreferences.Editor = sharedPrefs.edit()
                if (value != null) {
                    editor.putStringSet(PREF_TEAM_IDS, value)
                    editor.commit()
                } else {
                    editor.remove(PREF_TEAM_IDS)
                    editor.commit()
                }
            }
        }

    fun leaveTeam() {
        synchronized( this) {
            teamUuids = null
            uuidString =
                getNewUniqueId()
            isStarted.value?.let {
                if (it) {
                    stop()
                    start(isBackground)
                }
            }
        }
    }

    fun isTeamMember(scannnedUuid: String): Boolean {
        synchronized(this) {
            return teamUuids?.let {
                it.toTypedArray().contains(scannnedUuid)
            } ?: false
        }
    }

    fun isIosUuid(scannedUuid: String) : Boolean {
        return scannedUuid.substring(0..3) == Constants.IOS_SERVICE_STRING.substring(0..3)
    }

    fun getNewUniqueId() : String {
        val stringChars = (('0'..'9') + ('a'..'f')).toList().toTypedArray()
        val id = "0${((1..11).map { stringChars.random() }.joinToString(""))}"
        return Constants.ANDROID_SERVICE_STRING.replaceAfterLast('-', id)
    }

    lateinit var deviceNameServiceUuid: UUID

    fun start(startingBackground: Boolean) {
        synchronized(this) {
            if (isStarted.value!!) stop()
            if (startingBackground) startBackground() else startForeground()
        }
    }

    fun stop() {
        synchronized(this) {
            if (isBackground) stopBackground() else stopForeground()
        }
    }

    /*
     * The following methods deal with the problem that your intent that you use to stop
     * an alarm manager has to be identical to the intent that you used to stop it.  So
     * for that to be true you have to cancel the alarm with the correct argument for
     * background vs foreground, and thus a bunch of code...
     */
    private fun startBackground() {
        if (isEnabled() && !isPaused) {
            Log.i(TAG, "startBackground")
            isBackground = true
            isStarted.postValue(true)
            mBleServer.enable(Constants.REBROADCAST_PERIOD,
                context
            )
            mBleClient.enable(Constants.BACKGROUND_TRACE_INTERVAL,
                context
            )
        } else {
            Log.i(TAG, "startBackground not possible");
            isStarted.postValue(false)
        }
    }

    private fun startForeground() {
        if (isEnabled() && !isPaused) {
            Log.i(TAG, "startForeground")
            isBackground = false
            isStarted.postValue(true)
            mBleServer.enable(Constants.REBROADCAST_PERIOD,
                context
            )
            mBleClient.enable(Constants.FOREGROUND_TRACE_INTERVAL,
                context
            )
        } else {
            isStarted.postValue(false)
        }
    }

    private fun stopBackground() {
        if (isEnabled()) {
            mBleServer.disable(Constants.REBROADCAST_PERIOD,
                context
            )
            mBleClient.disable(Constants.BACKGROUND_TRACE_INTERVAL,
                context
            )
        }
        isStarted.postValue(false)
    }

    private fun stopForeground() {
        if (isEnabled()) {
            mBleServer.disable(Constants.REBROADCAST_PERIOD,
                context
            )
            mBleClient.disable(Constants.FOREGROUND_TRACE_INTERVAL,
                context
            )
        }
        isStarted.postValue(false)
    }



    fun isEnabled() : Boolean {
        bluetoothManager?.let {
            if (uuidString == null || it.adapter == null || !it.adapter.isEnabled()) return false
        }

        if (!isInit) init(
            context,
            this.isReactNative
        ) // If bluetooth was off we need to complete the init

        return isInit  // && isLocationEnabled() Location doesn't need to be on
    }

    fun init(applicationContext: Context, isReactNative: Boolean = false) {
        synchronized(this) {
            context = applicationContext
            this.isReactNative = isReactNative
            if (!isReactNative) {
                DeviceRepository.init(applicationContext)
            }


            if (!isInit && uuidString != null) {
                deviceNameServiceUuid = UUID.fromString(
                    uuidString
                )

                bluetoothManager =
                    context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                bluetoothManager?.let {
                    if (it.adapter == null || !it.adapter.isEnabled()) return // bail if bluetooth isn't on
                    bluetoothLeScanner = it.adapter.bluetoothLeScanner
                    bluetoothGattServer =
                        it.openGattServer(
                            context,
                            GattServerCallback
                        )
                    bluetoothLeAdvertiser = it.adapter.bluetoothLeAdvertiser
                }

                isInit = true

                // If we weren't paused we're started and in the background...
                if (!isPaused) isStarted.postValue(true) else isStarted.postValue(false)
                isBackground = true
            }
        }

        if (!isReactNative) {
            NotificationUtils.init(applicationContext)
        }

    }

    fun getAlarmManager(applicationContext: Context): AlarmManager {
        return applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }
}