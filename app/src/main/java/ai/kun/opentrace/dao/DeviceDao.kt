package ai.kun.opentrace.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device_table ORDER BY time_stamp_nanos DESC")
    fun getAllDevices(): LiveData<List<Device>>

    //TODO: Make this range query work correctly so we can stop deleting all the devices.
    //@Query("SELECT * FROM device_table WHERE time_stamp BETWEEN :startTime AND :endTime ORDER BY rssi DESC")
    @Query("SELECT * FROM device_table ORDER BY rssi DESC")
    fun getAllDevicesOrderByRssi(): LiveData<List<Device>>

    @Query("DELETE FROM device_table")
    fun deleteAllDevices()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)
}