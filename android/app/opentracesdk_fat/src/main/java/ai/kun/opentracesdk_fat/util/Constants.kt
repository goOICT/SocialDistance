package ai.kun.opentracesdk_fat.util

import java.util.*

/**
 * Constants used in the library.  There's a bunch of stuff in here you may want to tweak
 * to use it in your own app.
 */
object Constants {
    const val INTENT_DEVICE_SCANNED = "ai.kun.socialdistancealarm.device_scanned"
    const val PREF_TEAM_IDS = "team_ids"
    const val PREF_UNIQUE_ID = "unique_uuid"
    const val PREF_IS_PAUSED = "is_paused"
    const val PREF_FILE_NAME = "ai.kun.socialdistancealarm.preferencesV2"

    const val ANDROID_MANUFACTURE_SUBSTRING = "9b3"
    const val ANDROID_MANUFACTURE_SUBSTRING_MASK = "000"
    const val ANDROID_MANUFACTURE_ID = 1023
    const val APPLE_DEVICE_NAME = "SDAlarm"

    const val ANDROID_SERVICE_STRING = "d2b86f9a-264e-3ac6-838a-0d00c1f549ed"
    const val IOS_SERVICE_STRING =     "00086f9a-264e-3ac6-838a-0d00c1f549ed" //00086f9a-264e-3ac6-838a-6c57473bb945
    var ANDROID_PREFIX = ANDROID_SERVICE_STRING.subSequence(0..7 )
    var IOS_PREFIX = IOS_SERVICE_STRING.subSequence(0..7 )

    const val CHARACTERISTIC_DEVICE_STRING = "9a161ec7-72bb-40d3-b00c-fcf637349b5b"
    var CHARACTERISTIC_DEVICE_UUID =
        UUID.fromString(CHARACTERISTIC_DEVICE_STRING)
    const val CHARACTERISTIC_USER_STRING = "57be15c2-4776-4af6-b2af-c24d9c8711c5"
    var CHARACTERISTIC_USER_UUID =
        UUID.fromString(CHARACTERISTIC_USER_STRING)

    const val CLIENT_CONFIGURATION_DESCRIPTOR_SHORT_ID = "8ea1"
    const val SCAN_PERIOD: Long = 4000
    const val REBROADCAST_PERIOD = 30000
    const val BACKGROUND_TRACE_INTERVAL = 10000
    const val FOREGROUND_TRACE_INTERVAL = 10000

    const val SIGNAL_DISTANCE_OK = 23 // This or lower is socially distant = green
    const val SIGNAL_DISTANCE_LIGHT_WARN = 37 // This to SIGNAL_DISTANCE_OK warning = yellow
    const val SIGNAL_DISTANCE_STRONG_WARN = 52 // This to SIGNAL_DISTANCE_LIGHT_WARN strong warning = orange
    // ...and above this is not socially distant = red

    const val ASSUMED_TX_POWER = 127
    const val IOS_SIGNAL_REDUCTION = 15

    const val TIME_FORMAT = "h:mm a, d MMM"
}