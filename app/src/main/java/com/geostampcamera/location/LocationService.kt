package com.geostampcamera.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

// Holds resolved location data including human-readable address
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val accuracy: Float,
    val address: String,
    val city: String
)

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    // Fetch the current device location with high accuracy
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LocationData? = withContext(Dispatchers.IO) {
        try {
            val location = suspendCancellableCoroutine<Location?> { cont ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { loc ->
                    cont.resume(loc)
                }.addOnFailureListener {
                    cont.resume(null)
                }
            } ?: return@withContext null

            val address = reverseGeocode(location.latitude, location.longitude)

            LocationData(
                latitude = location.latitude,
                longitude = location.longitude,
                altitude = location.altitude,
                accuracy = location.accuracy,
                address = address?.getAddressLine(0) ?: "",
                city = address?.locality ?: address?.subAdminArea ?: ""
            )
        } catch (e: Exception) {
            null
        }
    }

    // Convert coordinates to a human-readable address
    @Suppress("DEPRECATION")
    private suspend fun reverseGeocode(
        latitude: Double,
        longitude: Double
    ): Address? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                suspendCancellableCoroutine { cont ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        cont.resume(addresses.firstOrNull())
                    }
                }
            } else {
                geocoder.getFromLocation(latitude, longitude, 1)?.firstOrNull()
            }
        } catch (e: Exception) {
            null
        }
    }
}
