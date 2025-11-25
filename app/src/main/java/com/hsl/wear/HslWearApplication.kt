package com.hsl.wear

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for HSL Wear OS app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class HslWearApplication : Application()
