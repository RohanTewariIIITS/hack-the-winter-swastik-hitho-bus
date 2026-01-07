package com.example.celltowertrackingforbus.RoomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TowersDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTower(tower: Tower)
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTowers(towers: List<Tower>)
    @Query("SELECT COUNT(*) FROM Tower")
    suspend fun getTowerCount(): Int

    @Query("DELETE FROM Tower")
    suspend fun deleteEntries()

    @Query("SELECT * FROM Tower WHERE mcc = :mcc AND mnc = :mnc AND lac = :lac AND cid = :cid LIMIT 1")
    suspend fun findTower(mcc: Int, mnc: Int, lac: Int, cid: Long): Tower?


}