package com.example.ap

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ParkingLocation(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val latitude: Double,
    val longitude: Double,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
