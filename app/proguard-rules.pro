# ProGuard / R8 rules for GeoStamp Camera

# Keep all app model / data classes and enums (critical for DataStore & Gson)
-keep class com.geostampcamera.data.model.** { *; }
-keep class com.geostampcamera.data.remote.** { *; }
-keep class com.geostampcamera.location.LocationData { *; }
-keep class com.geostampcamera.weather.WeatherData { *; }
-keep class com.geostampcamera.weather.RateLimitException { *; }
-keep class com.geostampcamera.sensor.CompassData { *; }

# Keep Retrofit interfaces and prevent stripping of annotations
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson TypeToken (critical for serialization)
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.internal.**

# Keep CameraX classes
-keep class androidx.camera.** { *; }

# Keep Google Maps classes
-keep class com.google.android.gms.maps.** { *; }

# Keep Google Play Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# Keep MLKit classes
-keep class com.google.mlkit.** { *; }

# Keep Coroutines internal machinery
-dontwarn kotlinx.coroutines.**
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Keep DataStore
-keep class androidx.datastore.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# General Android rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes InnerClasses,EnclosingMethod

# Keep enums (critical — R8 can strip enum entries)
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
