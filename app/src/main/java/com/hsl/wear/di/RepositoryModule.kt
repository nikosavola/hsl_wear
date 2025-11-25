package com.hsl.wear.di

import com.hsl.wear.data.repository.HslRepository
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.data.store.RouteStore
import com.hsl.wear.network.GeocodingClient
import com.hsl.wear.network.GraphQLClient
import com.hsl.wear.tiles.TileUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing repository dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideHslRepository(
        graphQLClient: GraphQLClient,
        geocodingClient: GeocodingClient
    ): HslRepository {
        return HslRepository(graphQLClient, geocodingClient)
    }

    @Provides
    @Singleton
    fun provideTransitRepository(
        hslRepository: HslRepository,
        routeStore: RouteStore,
        tileUpdater: TileUpdater
    ): TransitRepository {
        return TransitRepository(hslRepository, routeStore, tileUpdater)
    }
}
