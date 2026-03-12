package com.geostampcamera.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.geostampcamera.data.model.MapProvider
import com.geostampcamera.data.model.MapSize
import com.geostampcamera.data.model.MapType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.floor

// Result of a Google Maps API key validation
sealed class MapKeyValidationResult {
    data object Valid : MapKeyValidationResult()
    data class Invalid(val reason: String) : MapKeyValidationResult()
}

@Singleton
class MapSnapshotGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Validate a Google Maps Static API key by making a small test request
    suspend fun validateGoogleMapsKey(apiKey: String): MapKeyValidationResult =
        withContext(Dispatchers.IO) {
            if (apiKey.isBlank()) {
                return@withContext MapKeyValidationResult.Invalid("API key is empty")
            }
            try {
                val url = "https://maps.googleapis.com/maps/api/staticmap" +
                        "?center=0,0&zoom=1&size=64x64&key=$apiKey"
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.requestMethod = "GET"
                val code = connection.responseCode
                connection.disconnect()

                when {
                    code == 200 -> MapKeyValidationResult.Valid
                    code == 403 -> MapKeyValidationResult.Invalid("API key is invalid or restricted")
                    code == 429 -> MapKeyValidationResult.Invalid("Rate limit exceeded")
                    else -> MapKeyValidationResult.Invalid("HTTP error: $code")
                }
            } catch (e: Exception) {
                MapKeyValidationResult.Invalid("Network error: ${e.message}")
            }
        }

    // Generate a map snapshot bitmap with automatic fallback to OSM on failure
    suspend fun generateSnapshot(
        latitude: Double,
        longitude: Double,
        mapProvider: MapProvider,
        mapType: MapType,
        mapSize: MapSize,
        googleMapsApiKey: String
    ): Bitmap? = withContext(Dispatchers.IO) {
        when (mapProvider) {
            MapProvider.OPENSTREETMAP -> {
                fetchOsmSnapshotSafe(latitude, longitude, mapSize)
            }
            MapProvider.GOOGLE_MAPS -> {
                if (googleMapsApiKey.isBlank()) {
                    fetchOsmSnapshotSafe(latitude, longitude, mapSize)
                } else {
                    try {
                        fetchGoogleMapsSnapshot(
                            latitude, longitude, mapType, mapSize, googleMapsApiKey
                        )
                    } catch (e: Exception) {
                        // Any Google Maps failure falls back to OSM
                        fetchOsmSnapshotSafe(latitude, longitude, mapSize)
                    }
                }
            }
        }
    }

    // Safe wrapper that never throws
    private fun fetchOsmSnapshotSafe(
        latitude: Double,
        longitude: Double,
        mapSize: MapSize
    ): Bitmap? {
        return try {
            fetchOsmSnapshot(latitude, longitude, mapSize)
        } catch (e: Exception) {
            null
        }
    }

    // Fetch a map tile from OpenStreetMap and scale it to the requested size
    private fun fetchOsmSnapshot(
        latitude: Double,
        longitude: Double,
        mapSize: MapSize
    ): Bitmap? {
        val zoom = 15
        val xtile = floor((longitude + 180.0) / 360.0 * (1 shl zoom)).toInt()
        val ytile = floor(
            (1.0 - Math.log(
                Math.tan(Math.toRadians(latitude)) + 1.0 / Math.cos(Math.toRadians(latitude))
            ) / Math.PI) / 2.0 * (1 shl zoom)
        ).toInt()

        val url = "https://tile.openstreetmap.org/$zoom/$xtile/$ytile.png"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", "GeoStampCamera/1.0")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        val responseCode = connection.responseCode
        if (responseCode != 200) {
            connection.disconnect()
            return null
        }
        val inputStream = connection.inputStream
        val tile = BitmapFactory.decodeStream(inputStream) ?: return null
        inputStream.close()
        connection.disconnect()

        return Bitmap.createScaledBitmap(tile, mapSize.widthDp, mapSize.heightDp, true)
    }

    // Fetch a static map image from Google Maps Static API
    private fun fetchGoogleMapsSnapshot(
        latitude: Double,
        longitude: Double,
        mapType: MapType,
        mapSize: MapSize,
        apiKey: String
    ): Bitmap? {
        val googleMapType = when (mapType) {
            MapType.NORMAL -> "roadmap"
            MapType.SATELLITE -> "satellite"
            MapType.TERRAIN -> "terrain"
            MapType.HYBRID -> "hybrid"
        }
        val url = "https://maps.googleapis.com/maps/api/staticmap" +
                "?center=$latitude,$longitude" +
                "&zoom=15" +
                "&size=${mapSize.widthDp}x${mapSize.heightDp}" +
                "&maptype=$googleMapType" +
                "&markers=color:red%7C$latitude,$longitude" +
                "&key=$apiKey"

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10000
        connection.readTimeout = 10000
        val responseCode = connection.responseCode

        if (responseCode == 429 || responseCode == 403) {
            connection.disconnect()
            throw Exception("Google Maps API error: $responseCode")
        }

        if (responseCode != 200) {
            connection.disconnect()
            return null
        }

        val inputStream = connection.inputStream
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream.close()
        connection.disconnect()
        return bitmap
    }
}
