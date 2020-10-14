package ai.kun.opentracesdk_fat.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * When used natively the application uses a database to record detections.  That allows us to use
 * Room with a view.
 *
 * @property deviceUuid The UUID of the device detected
 * @property rssi The RSSI value
 * @property txPower The transmission strength
 * @property timeStampNanos The timestamp in nanoseconds
 * @property timeStamp The timestamp
 * @property sessionId The session ID
 * @property isTeamMember True if the detected handset was a team member at the time of detection
 * @property isAndroid True if the device was android, false if the device was iOS
 */
@Entity(tableName = "device_table")
data class Device(

    @ColumnInfo(name = "device_uuid")
    val deviceUuid: String,

    @ColumnInfo(name = "rssi")
    var rssi: Int,

    @ColumnInfo(name = "tx_power")
    var txPower: Int,

    @ColumnInfo(name = "time_stamp_nanos")
    val timeStampNanos: Long,

    @ColumnInfo(name = "time_stamp")
    val timeStamp: Long,

    @PrimaryKey
    @ColumnInfo(name = "session_id")
    val sessionId: String,

    @ColumnInfo(name = "is_team_member")
    val isTeamMember: Boolean,

    @ColumnInfo(name = "is_android")
    val isAndroid: Boolean
)