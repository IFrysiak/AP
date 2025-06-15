package com.example.ap

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ParkingLocation::class], version = 1)
abstract class ParkingLocationDatabase : RoomDatabase() {
    abstract fun parkingLocationDao(): ParkingLocationDao

    companion object {
        @Volatile
        private var INSTANCE: ParkingLocationDatabase? = null

        fun getDatabase(context: Context): ParkingLocationDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ParkingLocationDatabase::class.java,
                    "parking_location_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
