package com.dicoding.dicodingevent.data.local.room

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dicoding.dicodingevent.data.local.entity.EventEntity

@Dao
interface EventDao {
    @Query("SELECT * FROM event where favorite = 1")
    fun getFavoriteEvent(): LiveData<List<EventEntity>>

    @Query("SELECT * FROM event WHERE id = :eventId LIMIT 1")
    suspend fun getEventById(eventId: Int): EventEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: List<EventEntity>)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Delete
    suspend fun deleteEvent(event: EventEntity)

    @Query("SELECT EXISTS(SELECT * FROM event WHERE id = :eventId AND favorite = 1)")
    suspend fun isEventFavorite(eventId: Int): Boolean
}