package ai.kun.opentracesdk_fat.dao

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

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