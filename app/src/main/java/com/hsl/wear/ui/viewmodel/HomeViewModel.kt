package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.ui.models.HomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        checkActiveRoute()
    }

    private fun checkActiveRoute() {
        viewModelScope.launch {
            try {
                val activeRoute = transitRepository.activeRouteState.first()
                _uiState.value = _uiState.value.copy(
                    hasActiveRoute = activeRoute != null,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun refreshActiveRoute() {
        checkActiveRoute()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}