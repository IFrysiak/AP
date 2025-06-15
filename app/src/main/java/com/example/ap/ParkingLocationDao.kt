package com.example.ap

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ParkingLocationDao {
    @Query("SELECT * FROM parkinglocation ORDER BY timestamp DESC")
    suspend fun getAllLocations(): List<ParkingLocation>

    @Insert
    suspend fun insert(location: ParkingLocation)

    @Query("SELECT * FROM parkinglocation ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLocation(): ParkingLocation?
}

