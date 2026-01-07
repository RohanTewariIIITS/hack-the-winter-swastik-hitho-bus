package com.example.celltowertrackingforbus.RoomDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tower(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    var mcc: Int,
    val mnc: Int,
    val lac: Int,
    val cid: Long,
    val lat: Double,
    val long: Double
)