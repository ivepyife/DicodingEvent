package com.dicoding.dicodingevent.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.local.room.EventDao
import com.dicoding.dicodingevent.data.remote.retrofit.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EventRepository private constructor(
    private val apiService: ApiService,
    private val eventDao: EventDao
) {
    fun getAllEvents(active: Int): LiveData<Result<List<EventEntity>>> = liveData {
        emit(Result.Loading)
        try {
            val response = apiService.getEvents(active)

            val data = response.listEvents
            val eventList = data.map { event ->
                val isActive = active == 0
                val isFavourite = event.id.let {
                    eventDao.isEventFavorite(it)
                }
                EventEntity(
                    id = event.id,
                    name = event.name,
                    beginTime = event.beginTime,
                    imageLogo = event.imageLogo,
                    summary = event.summary,
                    ownerName = event.ownerName,
                    mediaCover = event.mediaCover,
                    registrants = event.registrants,
                    link = event.link,
                    description = event.description,
                    cityName = event.cityName,
                    quota = event.quota,
                    endTime = event.endTime,
                    category = event.category,
                    isFavorite = isFavourite,
                    isActive = isActive
                )
            }

            eventDao.insertEvent(eventList)
            emit(Result.Success(eventList))

        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getEventById(eventId: Int): LiveData<Result<EventEntity>> = liveData {
        emit(Result.Loading)
        try {
            val event = eventDao.getEventById(eventId)
            if (event != null) {
                emit(Result.Success(event))
            } else {
                val response = apiService.getDetailEvent(eventId)
                val eventData = response.event

                val eventEntity = EventEntity(
                    id = eventData.id,
                    name = eventData.name,
                    beginTime = eventData.beginTime,
                    imageLogo = eventData.imageLogo,
                    summary = eventData.summary,
                    ownerName = eventData.ownerName,
                    mediaCover = eventData.mediaCover,
                    registrants = eventData.registrants,
                    link = eventData.link,
                    description = eventData.description,
                    cityName = eventData.cityName,
                    quota = eventData.quota,
                    endTime = eventData.endTime,
                    category = eventData.category,
                    isFavorite = false,
                    isActive = true
                )
                emit(Result.Success(eventEntity))
            }
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun getFavouriteEvent(): LiveData<List<EventEntity>> {
        return eventDao.getFavoriteEvent()
    }

    suspend fun setFavoriteEvent(event: EventEntity, favoriteState: Boolean) {
        withContext(Dispatchers.IO) {
            event.isFavorite = favoriteState
            eventDao.updateEvent(event)
        }
    }

    suspend fun deleteEvent(event: EventEntity) {
        withContext(Dispatchers.IO) {
            eventDao.deleteEvent(event)
        }
    }

    companion object {
        @Volatile
        private var instance: EventRepository? = null
        fun getInstance(
            apiService: ApiService,
            eventDao: EventDao
        ): EventRepository =
            instance ?: synchronized(this) {
                instance ?: EventRepository(apiService, eventDao)
            }.also { instance = it }
    }
}
