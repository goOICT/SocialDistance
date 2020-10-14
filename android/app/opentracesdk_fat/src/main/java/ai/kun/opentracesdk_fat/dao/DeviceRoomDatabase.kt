package ai.kun.opentracesdk_fat.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope

/**
 * Room database for devices
 *
 */
@Database(entities = arrayOf(Device::class), version = 7, exportSchema = false)
public abstract class DeviceRoomDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var INSTANCE: DeviceRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): DeviceRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DeviceRoomDatabase::class.java,
                    "device_database"
                )

                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}