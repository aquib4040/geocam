# ProGuard / R8 rules for GeoStamp Camera

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# Keep Gson model classes
-keep class com.geostampcamera.data.remote.** { *; }

# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep CameraX classes
-keep class androidx.camera.** { *; }

# Keep Google Maps classes
-keep class com.google.android.gms.maps.** { *; }

# Keep MLKit classes
-keep class com.google.mlkit.** { *; }

# Keep Coroutines
-dontwarn kotlinx.coroutines.**

# Keep DataStore
-keep class androidx.datastore.** { *; }

# General Android rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
