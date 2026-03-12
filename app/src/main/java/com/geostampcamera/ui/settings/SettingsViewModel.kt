package com.geostampcamera.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geostampcamera.data.model.AppSettings
import com.geostampcamera.data.model.AppTheme
import com.geostampcamera.data.model.AspectRatio
import com.geostampcamera.data.model.FontSize
import com.geostampcamera.data.model.ImageQuality
import com.geostampcamera.data.model.MapProvider
import com.geostampcamera.data.model.MapSize
import com.geostampcamera.data.model.MapType
import com.geostampcamera.data.model.NamingStyle
import com.geostampcamera.data.model.StampFontColor
import com.geostampcamera.data.model.StampTemplate
import com.geostampcamera.data.model.StampTextAlignment
import com.geostampcamera.data.model.TemperatureUnit
import com.geostampcamera.data.model.TimerOption
import com.geostampcamera.data.model.WeatherProvider
import com.geostampcamera.data.repository.SettingsRepository
import com.geostampcamera.maps.MapKeyValidationResult
import com.geostampcamera.maps.MapSnapshotGenerator
import com.geostampcamera.weather.ApiKeyValidationResult
import com.geostampcamera.weather.WeatherService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// State for API key validation feedback
data class ApiKeyTestState(
    val isTesting: Boolean = false,
    val resultMessage: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val weatherService: WeatherService,
    private val mapSnapshotGenerator: MapSnapshotGenerator
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // API key validation state
    private val _weatherKeyTestState = MutableStateFlow(ApiKeyTestState())
    val weatherKeyTestState: StateFlow<ApiKeyTestState> = _weatherKeyTestState.asStateFlow()

    private val _mapKeyTestState = MutableStateFlow(ApiKeyTestState())
    val mapKeyTestState: StateFlow<ApiKeyTestState> = _mapKeyTestState.asStateFlow()

    // Validate OpenWeather API key
    fun testOpenWeatherKey(apiKey: String) {
        viewModelScope.launch {
            _weatherKeyTestState.value = ApiKeyTestState(isTesting = true)
            val result = weatherService.validateOpenWeatherKey(apiKey)
            _weatherKeyTestState.value = when (result) {
                is ApiKeyValidationResult.Valid -> ApiKeyTestState(
                    isTesting = false, resultMessage = "API key is valid", isSuccess = true
                )
                is ApiKeyValidationResult.Invalid -> ApiKeyTestState(
                    isTesting = false, resultMessage = result.reason, isSuccess = false
                )
            }
        }
    }

    // Validate Google Maps Static API key
    fun testGoogleMapsKey(apiKey: String) {
        viewModelScope.launch {
            _mapKeyTestState.value = ApiKeyTestState(isTesting = true)
            val result = mapSnapshotGenerator.validateGoogleMapsKey(apiKey)
            _mapKeyTestState.value = when (result) {
                is MapKeyValidationResult.Valid -> ApiKeyTestState(
                    isTesting = false, resultMessage = "API key is valid", isSuccess = true
                )
                is MapKeyValidationResult.Invalid -> ApiKeyTestState(
                    isTesting = false, resultMessage = result.reason, isSuccess = false
                )
            }
        }
    }

    // Clear test states
    fun clearWeatherKeyTestState() {
        _weatherKeyTestState.value = ApiKeyTestState()
    }

    fun clearMapKeyTestState() {
        _mapKeyTestState.value = ApiKeyTestState()
    }

    // -- Stamp content --
    fun setShowLatitude(v: Boolean) = launch { repository.setShowLatitude(v) }
    fun setShowLongitude(v: Boolean) = launch { repository.setShowLongitude(v) }
    fun setShowAddress(v: Boolean) = launch { repository.setShowAddress(v) }
    fun setShowDate(v: Boolean) = launch { repository.setShowDate(v) }
    fun setShowTime(v: Boolean) = launch { repository.setShowTime(v) }
    fun setShowWeather(v: Boolean) = launch { repository.setShowWeather(v) }
    fun setShowAltitude(v: Boolean) = launch { repository.setShowAltitude(v) }
    fun setShowCompass(v: Boolean) = launch { repository.setShowCompass(v) }
    fun setShowMapPreview(v: Boolean) = launch { repository.setShowMapPreview(v) }
    fun setShowCustomText(v: Boolean) = launch { repository.setShowCustomText(v) }
    fun setCustomText(v: String) = launch { repository.setCustomText(v) }

    // -- Template --
    fun setStampTemplate(v: StampTemplate) = launch { repository.setStampTemplate(v) }

    // -- Map --
    fun setMapProvider(v: MapProvider) = launch { repository.setMapProvider(v) }
    fun setMapType(v: MapType) = launch { repository.setMapType(v) }
    fun setMapSize(v: MapSize) = launch { repository.setMapSize(v) }
    fun setGoogleMapsApiKey(v: String) = launch { repository.setGoogleMapsApiKey(v) }

    // -- Text style --
    fun setFontSize(v: FontSize) = launch { repository.setFontSize(v) }
    fun setFontColor(v: StampFontColor) = launch { repository.setFontColor(v) }
    fun setTextAlignment(v: StampTextAlignment) = launch { repository.setTextAlignment(v) }
    fun setBackgroundOpacity(v: Float) = launch { repository.setBackgroundOpacity(v) }

    // -- Camera --
    fun setFlashEnabled(v: Boolean) = launch { repository.setFlashEnabled(v) }
    fun setGridEnabled(v: Boolean) = launch { repository.setGridEnabled(v) }
    fun setTimerOption(v: TimerOption) = launch { repository.setTimerOption(v) }
    fun setAspectRatio(v: AspectRatio) = launch { repository.setAspectRatio(v) }
    fun setMirrorMode(v: Boolean) = launch { repository.setMirrorMode(v) }
    fun setCaptureSound(v: Boolean) = launch { repository.setCaptureSound(v) }

    // -- Location --
    fun setUseAutoGps(v: Boolean) = launch { repository.setUseAutoGps(v) }
    fun setManualLatitude(v: Double) = launch { repository.setManualLatitude(v) }
    fun setManualLongitude(v: Double) = launch { repository.setManualLongitude(v) }

    // -- Weather --
    fun setWeatherProvider(v: WeatherProvider) = launch { repository.setWeatherProvider(v) }
    fun setTemperatureUnit(v: TemperatureUnit) = launch { repository.setTemperatureUnit(v) }
    fun setOpenWeatherApiKey(v: String) = launch { repository.setOpenWeatherApiKey(v) }

    // -- File save --
    fun setImageQuality(v: ImageQuality) = launch { repository.setImageQuality(v) }
    fun setNamingStyle(v: NamingStyle) = launch { repository.setNamingStyle(v) }
    fun setSaveFolder(v: String) = launch { repository.setSaveFolder(v) }

    // -- Theme --
    fun setAppTheme(v: AppTheme) = launch { repository.setAppTheme(v) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
