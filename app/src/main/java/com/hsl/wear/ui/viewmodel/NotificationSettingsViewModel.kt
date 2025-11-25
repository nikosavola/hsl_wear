package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.NotificationPreferences
import com.hsl.wear.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationPreferences())
    val uiState: StateFlow<NotificationPreferences> = _uiState.asStateFlow()

    init {
        loadNotificationPreferences()
    }

    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            try {
                val preferences = transitRepository.notificationPreferencesFlow.first()
                _uiState.value = preferences
            } catch (e: Exception) {
                // Use default preferences if loading fails
                _uiState.value = NotificationPreferences()
            }
        }
    }

    fun updatePreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            try {
                transitRepository.saveNotificationPreferences(preferences)
                _uiState.value = preferences
            } catch (e: Exception) {
                // Handle save error - could show error state
            }
        }
    }

    fun resetToDefaults() {
        val defaultPreferences = NotificationPreferences()
        updatePreferences(defaultPreferences)
    }
}