package com.dicoding.dicodingevent.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.dicoding.dicodingevent.data.local.entity.EventEntity
import com.dicoding.dicodingevent.data.source.EventRepository
import com.dicoding.dicodingevent.ui.setting.SettingPreferences
import kotlinx.coroutines.launch

class MainViewModel(
    private val eventRepository: EventRepository,
    private val pref: SettingPreferences
) : ViewModel() {

    // Theme Settings
    fun getThemeSettings(): LiveData<Boolean> {
        return pref.getThemeSetting().asLiveData()
    }

    fun saveThemeSetting(isDarkModeActive: Boolean) {
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkModeActive)
        }
    }

    // Events from Repository
    fun getAllEvents(active: Int) = eventRepository.getAllEvents(active)

    fun getFavoriteEvents() = eventRepository.getFavouriteEvent()

    suspend fun setFavoriteEvent(event: EventEntity, favoriteState: Boolean) {
        eventRepository.setFavoriteEvent(event, favoriteState)
    }

    suspend fun deleteEvent(event: EventEntity) {
        eventRepository.deleteEvent(event)
    }

    fun getEventById(eventId: Int) = eventRepository.getEventById(eventId)

    fun getNotificationSettings(): LiveData<Boolean> {
        return pref.getNotificationKey().asLiveData()
    }

    fun saveNotificationSetting(isNotificationActive: Boolean) {
        viewModelScope.launch {
            pref.saveNotificationSetting(isNotificationActive)
        }
    }
}
