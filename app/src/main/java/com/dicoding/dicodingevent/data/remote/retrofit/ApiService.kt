package com.dicoding.dicodingevent.data.remote.retrofit

import com.dicoding.dicodingevent.data.remote.respone.DetailEventResponse
import com.dicoding.dicodingevent.data.remote.respone.EventResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("events")
    suspend fun getEvents(
        @Query("active") active: Int,
        @Query("limit") limit: Int? = null  // Added limit parameter
    ): EventResponse

    @GET("events/{id}")
    suspend fun getDetailEvent(@Path("id") id: Int): DetailEventResponse
}