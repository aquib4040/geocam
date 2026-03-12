package com.geostampcamera.data.model

import android.graphics.Bitmap

// Holds all metadata collected at the moment of photo capture
data class PhotoMetadata(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val accuracy: Float = 0f,
    val address: String = "",
    val city: String = "",
    val date: String = "",
    val time: String = "",
    val temperature: String = "",
    val weatherDescription: String = "",
    val compassDirection: String = "",
    val compassDegrees: Float = 0f,
    val mapSnapshot: Bitmap? = null
)
