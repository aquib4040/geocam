package com.geostampcamera.data.repository

import com.geostampcamera.data.datastore.DataStoreManager
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
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStoreManager: DataStoreManager
) {
    val settingsFlow: Flow<AppSettings> = dataStoreManager.settingsFlow

    // -- Stamp content toggles --

    suspend fun setShowLatitude(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_LATITUDE, value)

    suspend fun setShowLongitude(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_LONGITUDE, value)

    suspend fun setShowAddress(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_ADDRESS, value)

    suspend fun setShowDate(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_DATE, value)

    suspend fun setShowTime(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_TIME, value)

    suspend fun setShowWeather(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_WEATHER, value)

    suspend fun setShowAltitude(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_ALTITUDE, value)

    suspend fun setShowCompass(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_COMPASS, value)

    suspend fun setShowMapPreview(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_MAP_PREVIEW, value)

    suspend fun setShowCustomText(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.SHOW_CUSTOM_TEXT, value)

    suspend fun setCustomText(value: String) =
        dataStoreManager.updateString(DataStoreManager.Keys.CUSTOM_TEXT, value)

    // -- Template --

    suspend fun setStampTemplate(value: StampTemplate) =
        dataStoreManager.updateInt(DataStoreManager.Keys.STAMP_TEMPLATE, value.ordinal)

    // -- Map --

    suspend fun setMapProvider(value: MapProvider) =
        dataStoreManager.updateInt(DataStoreManager.Keys.MAP_PROVIDER, value.ordinal)

    suspend fun setMapType(value: MapType) =
        dataStoreManager.updateInt(DataStoreManager.Keys.MAP_TYPE, value.ordinal)

    suspend fun setMapSize(value: MapSize) =
        dataStoreManager.updateInt(DataStoreManager.Keys.MAP_SIZE, value.ordinal)

    suspend fun setGoogleMapsApiKey(value: String) =
        dataStoreManager.updateString(DataStoreManager.Keys.GOOGLE_MAPS_API_KEY, value)

    suspend fun setMapplsApiKey(value: String) =
        dataStoreManager.updateString(DataStoreManager.Keys.MAPPLS_API_KEY, value)

    // -- Text style --

    suspend fun setFontSize(value: FontSize) =
        dataStoreManager.updateInt(DataStoreManager.Keys.FONT_SIZE, value.ordinal)

    suspend fun setFontColor(value: StampFontColor) =
        dataStoreManager.updateInt(DataStoreManager.Keys.FONT_COLOR, value.ordinal)

    suspend fun setTextAlignment(value: StampTextAlignment) =
        dataStoreManager.updateInt(DataStoreManager.Keys.TEXT_ALIGNMENT, value.ordinal)

    suspend fun setBackgroundOpacity(value: Float) =
        dataStoreManager.updateFloat(DataStoreManager.Keys.BACKGROUND_OPACITY, value)

    // -- Camera --

    suspend fun setFlashEnabled(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.FLASH_ENABLED, value)

    suspend fun setGridEnabled(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.GRID_ENABLED, value)

    suspend fun setTimerOption(value: TimerOption) =
        dataStoreManager.updateInt(DataStoreManager.Keys.TIMER_OPTION, value.ordinal)

    suspend fun setAspectRatio(value: AspectRatio) =
        dataStoreManager.updateInt(DataStoreManager.Keys.ASPECT_RATIO, value.ordinal)

    suspend fun setMirrorMode(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.MIRROR_MODE, value)

    suspend fun setCaptureSound(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.CAPTURE_SOUND, value)

    // -- Location --

    suspend fun setUseAutoGps(value: Boolean) =
        dataStoreManager.updateBoolean(DataStoreManager.Keys.USE_AUTO_GPS, value)

    suspend fun setManualLatitude(value: Double) =
        dataStoreManager.updateDouble(DataStoreManager.Keys.MANUAL_LATITUDE, value)

    suspend fun setManualLongitude(value: Double) =
        dataStoreManager.updateDouble(DataStoreManager.Keys.MANUAL_LONGITUDE, value)

    // -- Weather --

    suspend fun setWeatherProvider(value: WeatherProvider) =
        dataStoreManager.updateInt(DataStoreManager.Keys.WEATHER_PROVIDER, value.ordinal)

    suspend fun setTemperatureUnit(value: TemperatureUnit) =
        dataStoreManager.updateInt(DataStoreManager.Keys.TEMPERATURE_UNIT, value.ordinal)

    suspend fun setOpenWeatherApiKey(value: String) =
        dataStoreManager.updateString(DataStoreManager.Keys.OPENWEATHER_API_KEY, value)

    // -- File save --

    suspend fun setImageQuality(value: ImageQuality) =
        dataStoreManager.updateInt(DataStoreManager.Keys.IMAGE_QUALITY, value.ordinal)

    suspend fun setNamingStyle(value: NamingStyle) =
        dataStoreManager.updateInt(DataStoreManager.Keys.NAMING_STYLE, value.ordinal)

    suspend fun setSaveFolder(value: String) =
        dataStoreManager.updateString(DataStoreManager.Keys.SAVE_FOLDER, value)

    // -- Theme --

    suspend fun setAppTheme(value: AppTheme) =
        dataStoreManager.updateInt(DataStoreManager.Keys.APP_THEME, value.ordinal)
}
