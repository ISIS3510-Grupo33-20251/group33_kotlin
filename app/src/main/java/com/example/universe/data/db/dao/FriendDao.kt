package com.example.universe.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.universe.data.db.entity.FriendEntity

@Dao
interface FriendDao {
    @Query("SELECT * FROM friend")
    suspend fun getAllFriends(): List<FriendEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(friends: List<FriendEntity>)

    @Delete
    suspend fun delete(friend: FriendEntity)

    @Query("DELETE FROM friend WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM friend")
    suspend fun deleteAll()
}