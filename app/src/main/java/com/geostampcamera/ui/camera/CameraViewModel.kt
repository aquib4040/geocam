package com.geostampcamera.ui.camera

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geostampcamera.camera.CameraManager
import com.geostampcamera.data.model.AppSettings
import com.geostampcamera.data.model.PhotoMetadata
import com.geostampcamera.data.repository.PhotoRepository
import com.geostampcamera.data.repository.SettingsRepository
import com.geostampcamera.location.LocationService
import com.geostampcamera.maps.MapSnapshotGenerator
import com.geostampcamera.sensor.CompassData
import com.geostampcamera.sensor.CompassService
import com.geostampcamera.stamp.StampRenderer
import com.geostampcamera.weather.RateLimitException
import com.geostampcamera.weather.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CameraUiState(
    val isCapturing: Boolean = false,
    val timerCountdown: Int = 0,
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val lastPhotoPath: String? = null,
    val errorMessage: String? = null,
    val rateLimitMessage: String? = null,  // Shown as a dialog popup
    val usedFallback: Boolean = false       // If true, show subtle fallback indicator
)

@HiltViewModel
class CameraViewModel @Inject constructor(
    private val cameraManager: CameraManager,
    private val locationService: LocationService,
    private val weatherService: WeatherService,
    private val compassService: CompassService,
    private val mapSnapshotGenerator: MapSnapshotGenerator,
    private val stampRenderer: StampRenderer,
    private val photoRepository: PhotoRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private val _compassData = MutableStateFlow(CompassData(0f, "N"))
    val compassData: StateFlow<CompassData> = _compassData.asStateFlow()

    val cameraManagerInstance: CameraManager get() = cameraManager

    init {
        observeCompass()
    }

    private fun observeCompass() {
        viewModelScope.launch {
            compassService.observeCompass().collect { data ->
                _compassData.value = data
            }
        }
    }

    // Capture photo with full metadata collection and stamp rendering
    fun capturePhoto() {
        if (_uiState.value.isCapturing) return

        viewModelScope.launch {
            val currentSettings = settings.value

            // Handle timer countdown
            if (currentSettings.timerOption.seconds > 0) {
                for (i in currentSettings.timerOption.seconds downTo 1) {
                    _uiState.value = _uiState.value.copy(timerCountdown = i)
                    kotlinx.coroutines.delay(1000)
                }
                _uiState.value = _uiState.value.copy(timerCountdown = 0)
            }

            _uiState.value = _uiState.value.copy(
                isCapturing = true, errorMessage = null, usedFallback = false
            )

            try {
                // Step 1: Capture the raw photo
                val photoFile = cameraManager.capturePhoto(
                    playSound = currentSettings.captureSound
                )

                // Step 2: Collect all metadata
                val location = if (currentSettings.useAutoGps) {
                    locationService.getCurrentLocation()
                } else {
                    null
                }

                val lat = location?.latitude ?: currentSettings.manualLatitude
                val lng = location?.longitude ?: currentSettings.manualLongitude

                // Weather: handle rate limit with fallback
                var weatherUsedFallback = false
                val weather = try {
                    weatherService.getWeather(
                        latitude = lat,
                        longitude = lng,
                        provider = currentSettings.weatherProvider,
                        unit = currentSettings.temperatureUnit,
                        openWeatherApiKey = currentSettings.openWeatherApiKey
                    ).also { weatherUsedFallback = it.usedFallback }
                } catch (e: RateLimitException) {
                    // Rate limit hit - use fallback data but notify user
                    weatherUsedFallback = true
                    _uiState.value = _uiState.value.copy(
                        rateLimitMessage = "${e.provider} API rate limit exceeded.\n" +
                                "Using free provider (Open-Meteo) instead."
                    )
                    e.fallbackData
                }

                // Map snapshot: auto-falls back to OSM internally
                val mapSnapshot = if (currentSettings.showMapPreview) {
                    mapSnapshotGenerator.generateSnapshot(
                        latitude = lat,
                        longitude = lng,
                        mapProvider = currentSettings.mapProvider,
                        mapType = currentSettings.mapType,
                        mapSize = currentSettings.mapSize,
                        googleMapsApiKey = currentSettings.googleMapsApiKey
                    )
                } else null

                val compass = _compassData.value
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                val now = Date()

                val metadata = PhotoMetadata(
                    latitude = lat,
                    longitude = lng,
                    altitude = location?.altitude ?: 0.0,
                    accuracy = location?.accuracy ?: 0f,
                    address = location?.address ?: "",
                    city = location?.city ?: "",
                    date = dateFormat.format(now),
                    time = timeFormat.format(now),
                    temperature = weather.temperature,
                    weatherDescription = weather.description,
                    compassDirection = compass.direction,
                    compassDegrees = compass.degrees,
                    mapSnapshot = mapSnapshot
                )

                // Step 3: Load bitmap and apply mirror if needed
                var photoBitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                if (currentSettings.mirrorMode &&
                    _uiState.value.lensFacing == CameraSelector.LENS_FACING_FRONT
                ) {
                    val matrix = Matrix().apply { preScale(-1f, 1f) }
                    photoBitmap = Bitmap.createBitmap(
                        photoBitmap, 0, 0,
                        photoBitmap.width, photoBitmap.height,
                        matrix, true
                    )
                }

                // Step 4: Render stamp onto the photo
                val stampedBitmap = stampRenderer.renderStamp(
                    photoBitmap, metadata, currentSettings
                )

                // Step 5: Save to gallery with EXIF data
                val savedUri = photoRepository.saveStampedPhoto(
                    stampedBitmap, metadata, currentSettings
                )

                // Clean up temp file
                photoFile.delete()

                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    lastPhotoPath = savedUri,
                    usedFallback = weatherUsedFallback
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    errorMessage = e.message ?: "Failed to capture photo"
                )
            }
        }
    }

    // Switch between front and back camera
    fun toggleCamera() {
        val newFacing = cameraManager.getOppositeLensFacing()
        _uiState.value = _uiState.value.copy(lensFacing = newFacing)
    }

    // Update flash setting and propagate to camera hardware
    fun toggleFlash() {
        viewModelScope.launch {
            val newValue = !settings.value.flashEnabled
            settingsRepository.setFlashEnabled(newValue)
            cameraManager.setFlashMode(newValue)
        }
    }

    // Toggle the camera grid overlay
    fun toggleGrid() {
        viewModelScope.launch {
            settingsRepository.setGridEnabled(!settings.value.gridEnabled)
        }
    }

    // Clear any displayed error
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Dismiss the rate limit popup
    fun dismissRateLimitMessage() {
        _uiState.value = _uiState.value.copy(rateLimitMessage = null)
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}
