package com.hsl.wear.di

import com.hsl.wear.network.GeocodingClient
import com.hsl.wear.network.GraphQLClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing network-related dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGraphQLClient(): GraphQLClient {
        return GraphQLClient()
    }

    @Provides
    @Singleton
    fun provideGeocodingClient(): GeocodingClient {
        return GeocodingClient()
    }
}
