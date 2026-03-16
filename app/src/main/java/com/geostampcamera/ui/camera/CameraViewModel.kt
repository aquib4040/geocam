package com.geostampcamera.ui.camera

import androidx.camera.core.CameraSelector
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    val usedFallback: Boolean = false,      // If true, show subtle fallback indicator
    val previewBitmap: android.graphics.Bitmap? = null, // Current rendered preview
    val isProcessingStamp: Boolean = false
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

    private var rawBitmap: android.graphics.Bitmap? = null
    private var currentMetadata: PhotoMetadata? = null

    init {
        observeCompass()
        observeSettingsForPreview()
    }

    private fun observeSettingsForPreview() {
        viewModelScope.launch {
            settings.collect { currentSettings ->
                // If we have a raw bitmap and metadata, re-render the preview when settings change
                if (rawBitmap != null && currentMetadata != null) {
                    renderPreview(rawBitmap!!, currentMetadata!!, currentSettings)
                }
            }
        }
    }

    private fun observeCompass() {
        viewModelScope.launch {
            compassService.observeCompass().collect { data ->
                _compassData.value = data
            }
        }
    }

    // Capture photo with fast-capture logic
    fun capturePhoto() {
        if (_uiState.value.isCapturing) return

        viewModelScope.launch {
            val currentSettings = settings.value

            // Timer countdown
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
                // Step 1: Capture the raw photo immediately
                val photoFile = cameraManager.capturePhoto(
                    playSound = currentSettings.captureSound
                )

                // Load raw bitmap
                var photoBitmap = android.graphics.BitmapFactory.decodeFile(photoFile.absolutePath)
                if (currentSettings.mirrorMode &&
                    _uiState.value.lensFacing == CameraSelector.LENS_FACING_FRONT
                ) {
                    val matrix = android.graphics.Matrix().apply { preScale(-1f, 1f) }
                    photoBitmap = android.graphics.Bitmap.createBitmap(
                        photoBitmap, 0, 0,
                        photoBitmap.width, photoBitmap.height,
                        matrix, true
                    )
                }
                rawBitmap = photoBitmap
                photoFile.delete() // Don't need the temp file anymore

                // Immediately show the raw photo in preview (without stamp yet)
                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    isProcessingStamp = true,
                    previewBitmap = photoBitmap,
                    lastPhotoPath = "live_preview" // Flag for navigation
                )

                // Step 2: Collect all metadata in background
                val metadata = coroutineScope {
                    val locationDeferred = async {
                        if (currentSettings.useAutoGps) {
                            try {
                                locationService.getCurrentLocation()
                            } catch (_: Exception) {
                                null
                            }
                        } else {
                            null
                        }
                    }

                    val weatherDeferred = async {
                        val lat = (if (currentSettings.useAutoGps) null else currentSettings.manualLatitude)
                            ?: try { locationDeferred.await()?.latitude } catch(_:Exception) { null } 
                            ?: currentSettings.manualLatitude
                        val lng = (if (currentSettings.useAutoGps) null else currentSettings.manualLongitude)
                            ?: try { locationDeferred.await()?.longitude } catch(_:Exception) { null }
                            ?: currentSettings.manualLongitude

                        try {
                            weatherService.getWeather(
                                latitude = lat,
                                longitude = lng,
                                provider = currentSettings.weatherProvider,
                                unit = currentSettings.temperatureUnit,
                                openWeatherApiKey = currentSettings.openWeatherApiKey
                            )
                        } catch (e: RateLimitException) {
                            _uiState.value = _uiState.value.copy(
                                rateLimitMessage = "${e.provider} API rate limit exceeded.\n" +
                                        "Using free provider (Open-Meteo) instead."
                            )
                            e.fallbackData
                        } catch (_: Exception) {
                            null
                        }
                    }

                    val mapSnapshotDeferred = async {
                        if (currentSettings.showMapPreview) {
                            val loc = try { locationDeferred.await() } catch(_:Exception) { null }
                            val lat = loc?.latitude ?: currentSettings.manualLatitude
                            val lng = loc?.longitude ?: currentSettings.manualLongitude

                            mapSnapshotGenerator.generateSnapshot(
                                latitude = lat,
                                longitude = lng,
                                mapProvider = currentSettings.mapProvider,
                                mapType = currentSettings.mapType,
                                mapSize = currentSettings.mapSize,
                                googleMapsApiKey = currentSettings.googleMapsApiKey,
                                mapplsApiKey = currentSettings.mapplsApiKey
                            )
                        } else null
                    }

                    val location = locationDeferred.await()
                    val weather = weatherDeferred.await()
                    val mapSnapshot = mapSnapshotDeferred.await()

                    val lat = location?.latitude ?: currentSettings.manualLatitude
                    val lng = location?.longitude ?: currentSettings.manualLongitude
                    val compass = _compassData.value
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                    val now = Date()

                    PhotoMetadata(
                        latitude = lat,
                        longitude = lng,
                        altitude = location?.altitude ?: 0.0,
                        accuracy = location?.accuracy ?: 0f,
                        address = location?.address ?: if (lat == 0.0 && lng == 0.0) "Location unavailable" else "",
                        city = location?.city ?: "",
                        date = dateFormat.format(now),
                        time = timeFormat.format(now),
                        temperature = weather?.temperature ?: "--",
                        weatherDescription = weather?.description ?: "unavailable",
                        compassDirection = compass.direction,
                        compassDegrees = compass.degrees,
                        mapSnapshot = mapSnapshot
                    )
                }

                currentMetadata = metadata
                renderPreview(photoBitmap, metadata, currentSettings)
                _uiState.value = _uiState.value.copy(isProcessingStamp = false)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    isProcessingStamp = false,
                    errorMessage = e.message ?: "Failed to capture photo"
                )
            }
        }
    }

    private fun renderPreview(
        bitmap: android.graphics.Bitmap,
        metadata: PhotoMetadata,
        settings: AppSettings
    ) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            val stamped = stampRenderer.renderStamp(bitmap, metadata, settings)
            _uiState.value = _uiState.value.copy(previewBitmap = stamped)
        }
    }

    // Save the current previewed photo to gallery
    fun savePhoto() {
        val bitmap = _uiState.value.previewBitmap ?: return
        val metadata = currentMetadata ?: return
        val currentSettings = settings.value

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                photoRepository.saveStampedPhoto(bitmap, metadata, currentSettings)
                // Clear state after save
                rawBitmap = null
                currentMetadata = null
                _uiState.value = _uiState.value.copy(previewBitmap = null)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Failed to save: ${e.message}")
            }
        }
    }

    fun discardPhoto() {
        rawBitmap = null
        currentMetadata = null
        _uiState.value = _uiState.value.copy(previewBitmap = null, lastPhotoPath = null)
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

    // Clear last photo path after navigation
    fun clearLastPhotoPath() {
        if (_uiState.value.lastPhotoPath == "live_preview") {
            // Keep it as "live_preview" until we actually leave the preview screen
        } else {
            _uiState.value = _uiState.value.copy(lastPhotoPath = null)
        }
    }

    fun updateTemplate(template: com.geostampcamera.data.model.StampTemplate) {
        viewModelScope.launch {
            settingsRepository.setStampTemplate(template)
        }
    }

    // Reset everything when leaving preview
    fun exitPreview() {
        rawBitmap = null
        currentMetadata = null
        _uiState.value = _uiState.value.copy(previewBitmap = null, lastPhotoPath = null)
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager.release()
    }
}
