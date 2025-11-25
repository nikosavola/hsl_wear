package com.hsl.wear.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.hsl.wear.data.store.RouteStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing data persistence dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "route_prefs")

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideRouteStore(dataStore: DataStore<Preferences>): RouteStore {
        return RouteStore(dataStore)
    }
}
