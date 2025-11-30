# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========== Kotlin Serialization ==========
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-dontnote kotlinx.serialization.SerializationKt

# Keep serializer classes
-keep,includedescriptorclasses class com.hsl.wear.data.models.**$$serializer { *; }
-keepclassmembers class com.hsl.wear.data.models.** {
    *** Companion;
}
-keepclasseswithmembers class com.hsl.wear.data.models.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data model classes
-keep,includedescriptorclasses class com.hsl.wear.data.models.** {
    <init>(...);
}
-keepclassmembers class com.hsl.wear.data.models.** {
    <fields>;
}

# Specific fix for StoptimeWrapper serializer
-dontwarn com.hsl.wear.data.models.StoptimeWrapper$$serializer

# ========== OkHttp ==========
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ========== Hilt ==========
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class dagger.hilt.android.internal.managers.** { *; }

# ========== Jetpack Compose ==========
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.** {
    *;
}

# ========== Wear OS ==========
-keep class androidx.wear.** { *; }
-keepclassmembers class androidx.wear.** {
    *;
}

# ========== Coroutines ==========
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========== ViewModels ==========
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# ========== DataStore ==========
-keep class androidx.datastore.*.** { *; }

# ========== General Android ==========
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ========== Enums ==========
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}