package com.geostampcamera.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "geostamp_settings"
)

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Read settings as a reactive flow
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            showLatitude = prefs[Keys.SHOW_LATITUDE] ?: true,
            showLongitude = prefs[Keys.SHOW_LONGITUDE] ?: true,
            showAddress = prefs[Keys.SHOW_ADDRESS] ?: true,
            showDate = prefs[Keys.SHOW_DATE] ?: true,
            showTime = prefs[Keys.SHOW_TIME] ?: true,
            showWeather = prefs[Keys.SHOW_WEATHER] ?: true,
            showAltitude = prefs[Keys.SHOW_ALTITUDE] ?: true,
            showCompass = prefs[Keys.SHOW_COMPASS] ?: true,
            showMapPreview = prefs[Keys.SHOW_MAP_PREVIEW] ?: true,
            showCustomText = prefs[Keys.SHOW_CUSTOM_TEXT] ?: false,
            customText = prefs[Keys.CUSTOM_TEXT] ?: "",
            stampTemplate = enumByOrdinal(prefs[Keys.STAMP_TEMPLATE], StampTemplate.CLASSIC),
            mapProvider = enumByOrdinal(prefs[Keys.MAP_PROVIDER], MapProvider.OPENSTREETMAP),
            mapType = enumByOrdinal(prefs[Keys.MAP_TYPE], MapType.NORMAL),
            mapSize = enumByOrdinal(prefs[Keys.MAP_SIZE], MapSize.MEDIUM),
            googleMapsApiKey = prefs[Keys.GOOGLE_MAPS_API_KEY] ?: "",
            mapplsApiKey = prefs[Keys.MAPPLS_API_KEY] ?: "",
            fontSize = enumByOrdinal(prefs[Keys.FONT_SIZE], FontSize.MEDIUM),
            fontColor = enumByOrdinal(prefs[Keys.FONT_COLOR], StampFontColor.WHITE),
            textAlignment = enumByOrdinal(prefs[Keys.TEXT_ALIGNMENT], StampTextAlignment.LEFT),
            backgroundOpacity = prefs[Keys.BACKGROUND_OPACITY] ?: 0.7f,
            flashEnabled = prefs[Keys.FLASH_ENABLED] ?: false,
            gridEnabled = prefs[Keys.GRID_ENABLED] ?: false,
            timerOption = enumByOrdinal(prefs[Keys.TIMER_OPTION], TimerOption.OFF),
            aspectRatio = enumByOrdinal(prefs[Keys.ASPECT_RATIO], AspectRatio.RATIO_4_3),
            mirrorMode = prefs[Keys.MIRROR_MODE] ?: false,
            captureSound = prefs[Keys.CAPTURE_SOUND] ?: true,
            useAutoGps = prefs[Keys.USE_AUTO_GPS] ?: true,
            manualLatitude = prefs[Keys.MANUAL_LATITUDE] ?: 0.0,
            manualLongitude = prefs[Keys.MANUAL_LONGITUDE] ?: 0.0,
            weatherProvider = enumByOrdinal(prefs[Keys.WEATHER_PROVIDER], WeatherProvider.OPEN_METEO),
            temperatureUnit = enumByOrdinal(prefs[Keys.TEMPERATURE_UNIT], TemperatureUnit.CELSIUS),
            openWeatherApiKey = prefs[Keys.OPENWEATHER_API_KEY] ?: "",
            imageQuality = enumByOrdinal(prefs[Keys.IMAGE_QUALITY], ImageQuality.HIGH),
            namingStyle = enumByOrdinal(prefs[Keys.NAMING_STYLE], NamingStyle.CITY_DATE_TIME),
            saveFolder = prefs[Keys.SAVE_FOLDER] ?: "",
            appTheme = enumByOrdinal(prefs[Keys.APP_THEME], AppTheme.SYSTEM)
        )
    }

    // Generic boolean update
    suspend fun updateBoolean(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { it[key] = value }
    }

    // Generic int update (used for enum ordinals)
    suspend fun updateInt(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { it[key] = value }
    }

    // Generic float update
    suspend fun updateFloat(key: Preferences.Key<Float>, value: Float) {
        context.dataStore.edit { it[key] = value }
    }

    // Generic string update
    suspend fun updateString(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { it[key] = value }
    }

    // Generic double update
    suspend fun updateDouble(key: Preferences.Key<Double>, value: Double) {
        context.dataStore.edit { it[key] = value }
    }

    // Safely recover an enum value by ordinal index
    private inline fun <reified T : Enum<T>> enumByOrdinal(ordinal: Int?, default: T): T {
        val entries = enumValues<T>()
        return if (ordinal != null && ordinal in entries.indices) entries[ordinal] else default
    }

    // All DataStore preference keys
    object Keys {
        val SHOW_LATITUDE = booleanPreferencesKey("show_latitude")
        val SHOW_LONGITUDE = booleanPreferencesKey("show_longitude")
        val SHOW_ADDRESS = booleanPreferencesKey("show_address")
        val SHOW_DATE = booleanPreferencesKey("show_date")
        val SHOW_TIME = booleanPreferencesKey("show_time")
        val SHOW_WEATHER = booleanPreferencesKey("show_weather")
        val SHOW_ALTITUDE = booleanPreferencesKey("show_altitude")
        val SHOW_COMPASS = booleanPreferencesKey("show_compass")
        val SHOW_MAP_PREVIEW = booleanPreferencesKey("show_map_preview")
        val SHOW_CUSTOM_TEXT = booleanPreferencesKey("show_custom_text")
        val CUSTOM_TEXT = stringPreferencesKey("custom_text")
        val STAMP_TEMPLATE = intPreferencesKey("stamp_template")
        val MAP_PROVIDER = intPreferencesKey("map_provider")
        val MAP_TYPE = intPreferencesKey("map_type")
        val MAP_SIZE = intPreferencesKey("map_size")
        val GOOGLE_MAPS_API_KEY = stringPreferencesKey("google_maps_api_key")
        val MAPPLS_API_KEY = stringPreferencesKey("mappls_api_key")
        val FONT_SIZE = intPreferencesKey("font_size")
        val FONT_COLOR = intPreferencesKey("font_color")
        val TEXT_ALIGNMENT = intPreferencesKey("text_alignment")
        val BACKGROUND_OPACITY = floatPreferencesKey("background_opacity")
        val FLASH_ENABLED = booleanPreferencesKey("flash_enabled")
        val GRID_ENABLED = booleanPreferencesKey("grid_enabled")
        val TIMER_OPTION = intPreferencesKey("timer_option")
        val ASPECT_RATIO = intPreferencesKey("aspect_ratio")
        val MIRROR_MODE = booleanPreferencesKey("mirror_mode")
        val CAPTURE_SOUND = booleanPreferencesKey("capture_sound")
        val USE_AUTO_GPS = booleanPreferencesKey("use_auto_gps")
        val MANUAL_LATITUDE = doublePreferencesKey("manual_latitude")
        val MANUAL_LONGITUDE = doublePreferencesKey("manual_longitude")
        val WEATHER_PROVIDER = intPreferencesKey("weather_provider")
        val TEMPERATURE_UNIT = intPreferencesKey("temperature_unit")
        val OPENWEATHER_API_KEY = stringPreferencesKey("openweather_api_key")
        val IMAGE_QUALITY = intPreferencesKey("image_quality")
        val NAMING_STYLE = intPreferencesKey("naming_style")
        val SAVE_FOLDER = stringPreferencesKey("save_folder")
        val APP_THEME = intPreferencesKey("app_theme")
    }
}
