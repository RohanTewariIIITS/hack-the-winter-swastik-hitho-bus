package com.example.celltowertrackingforbus.RoomDatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Tower::class],
    version = 3,
    exportSchema = true
)
abstract class TowersDatabase: RoomDatabase() {

    abstract val dao: TowersDao

}