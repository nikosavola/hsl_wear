package com.hsl.wear.ui.models

data class HomeUiState(
    val hasActiveRoute: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)