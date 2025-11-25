package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.FavoriteRoute
import com.hsl.wear.data.repository.TransitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouriteRoutesViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    val favoriteRoutes: Flow<List<FavoriteRoute>> = transitRepository.favoriteRoutes

    fun removeFavoriteRoute(routeId: String) {
        viewModelScope.launch {
            transitRepository.removeFavoriteRoute(routeId)
        }
    }
}
