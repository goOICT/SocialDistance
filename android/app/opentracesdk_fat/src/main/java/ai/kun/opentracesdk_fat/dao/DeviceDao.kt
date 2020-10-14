package ai.kun.opentracesdk_fat.dao

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * a DAO for dealing with devices
 *
 */
@Dao
interface DeviceDao {
    /**
     * get all the devices that have been detected
     *
     * @return live data for all the devices
     */
    @Query("SELECT * FROM device_table ORDER BY time_stamp DESC")
    fun getAllDevices(): LiveData<List<Device>>

    /**
     * get the devices detected in the last scanning cycle ordered by signal strength.
     *
     * @param startTime start of the scanning period
     * @param endTime end of the scanning period
     * @return a list of the currently detected devices (not live data)
     */
    @Query("SELECT * FROM device_table WHERE time_stamp BETWEEN :startTime AND :endTime ORDER BY rssi DESC")
    fun getCurrentDevicesOrderByRssi(startTime: Long, endTime: Long): List<Device>

    /**
     * Delete all the devices in the database
     *
     */
    @Query("DELETE FROM device_table")
    fun deleteAllDevices()

    /**
     * add a new device to the database
     *
     * @param device
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)
}