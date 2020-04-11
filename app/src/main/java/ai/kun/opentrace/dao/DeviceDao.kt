package ai.kun.opentrace.dao

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DeviceDao {
    @Query("SELECT * FROM device_table ORDER BY time_stamp_nanos DESC")
    fun getDevicesSeen(): LiveData<List<Device>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(device: Device)
}