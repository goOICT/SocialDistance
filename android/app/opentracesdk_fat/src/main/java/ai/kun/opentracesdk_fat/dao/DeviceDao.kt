package ai.kun.opentracesdk_fat.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device_table ORDER BY time_stamp DESC")
    fun getAllDevices(): LiveData<List<Device>>


    @Query("SELECT * FROM device_table WHERE time_stamp BETWEEN :startTime AND :endTime ORDER BY rssi DESC")
    fun getCurrentDevicesOrderByRssi(startTime: Long, endTime: Long): List<Device>

    @Query("DELETE FROM device_table")
    fun deleteAllDevices()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)
}