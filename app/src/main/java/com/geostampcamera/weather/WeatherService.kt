package com.geostampcamera.weather

import com.geostampcamera.data.model.TemperatureUnit
import com.geostampcamera.data.model.WeatherProvider
import com.geostampcamera.data.remote.OpenMeteoApi
import com.geostampcamera.data.remote.OpenWeatherApi
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

// Resolved weather information
data class WeatherData(
    val temperature: String,
    val description: String,
    val usedFallback: Boolean = false
)

// Result of an API key validation attempt
sealed class ApiKeyValidationResult {
    data object Valid : ApiKeyValidationResult()
    data class Invalid(val reason: String) : ApiKeyValidationResult()
}

@Singleton
class WeatherService @Inject constructor(
    private val openMeteoApi: OpenMeteoApi,
    private val openWeatherApi: OpenWeatherApi
) {

    // Validate an OpenWeather API key by making a test request
    suspend fun validateOpenWeatherKey(apiKey: String): ApiKeyValidationResult {
        if (apiKey.isBlank()) {
            return ApiKeyValidationResult.Invalid("API key is empty")
        }
        return try {
            // Test with known coordinates (London)
            val response = openWeatherApi.getCurrentWeather(51.5074, -0.1278, apiKey)
            if (response.main != null) {
                ApiKeyValidationResult.Valid
            } else {
                ApiKeyValidationResult.Invalid("Invalid response from server")
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> ApiKeyValidationResult.Invalid("Invalid API key")
                429 -> ApiKeyValidationResult.Invalid("Rate limit exceeded")
                else -> ApiKeyValidationResult.Invalid("HTTP error: ${e.code()}")
            }
        } catch (e: Exception) {
            ApiKeyValidationResult.Invalid("Network error: ${e.message}")
        }
    }

    // Fetch weather data from the selected provider with automatic fallback
    suspend fun getWeather(
        latitude: Double,
        longitude: Double,
        provider: WeatherProvider,
        unit: TemperatureUnit,
        openWeatherApiKey: String
    ): WeatherData {
        return when (provider) {
            WeatherProvider.OPEN_METEO -> {
                try {
                    fetchOpenMeteo(latitude, longitude, unit)
                } catch (e: Exception) {
                    WeatherData("--", "Unavailable")
                }
            }
            WeatherProvider.OPENWEATHER -> {
                if (openWeatherApiKey.isBlank()) {
                    // No key provided, fallback silently
                    fetchOpenMeteoSafe(latitude, longitude, unit, fallback = true)
                } else {
                    try {
                        fetchOpenWeather(latitude, longitude, openWeatherApiKey, unit)
                    } catch (e: HttpException) {
                        when (e.code()) {
                            401 -> {
                                // Invalid key, fallback to free provider
                                fetchOpenMeteoSafe(latitude, longitude, unit, fallback = true)
                            }
                            429 -> {
                                // Rate limit exceeded, fallback with flag
                                val fallbackData = fetchOpenMeteoSafe(latitude, longitude, unit, fallback = true)
                                throw RateLimitException(
                                    provider = "OpenWeather",
                                    fallbackData = fallbackData
                                )
                            }
                            else -> {
                                fetchOpenMeteoSafe(latitude, longitude, unit, fallback = true)
                            }
                        }
                    } catch (e: RateLimitException) {
                        throw e // Re-throw rate limit exceptions
                    } catch (e: Exception) {
                        fetchOpenMeteoSafe(latitude, longitude, unit, fallback = true)
                    }
                }
            }
        }
    }

    // Safe wrapper that never throws, always returns data
    private suspend fun fetchOpenMeteoSafe(
        latitude: Double,
        longitude: Double,
        unit: TemperatureUnit,
        fallback: Boolean
    ): WeatherData {
        return try {
            val data = fetchOpenMeteo(latitude, longitude, unit)
            data.copy(usedFallback = fallback)
        } catch (e: Exception) {
            WeatherData("--", "Unavailable", usedFallback = fallback)
        }
    }

    // Fetch from Open-Meteo (free, no key)
    private suspend fun fetchOpenMeteo(
        latitude: Double,
        longitude: Double,
        unit: TemperatureUnit
    ): WeatherData {
        val response = openMeteoApi.getCurrentWeather(latitude, longitude)
        val current = response.current_weather
            ?: return WeatherData("--", "Unavailable")

        val tempCelsius = current.temperature
        val temp = when (unit) {
            TemperatureUnit.CELSIUS -> "${tempCelsius.toInt()}C"
            TemperatureUnit.FAHRENHEIT -> "${((tempCelsius * 9 / 5) + 32).toInt()}F"
        }
        val desc = wmoCodeToDescription(current.weathercode)
        return WeatherData(temperature = temp, description = desc)
    }

    // Fetch from OpenWeather (requires API key)
    private suspend fun fetchOpenWeather(
        latitude: Double,
        longitude: Double,
        apiKey: String,
        unit: TemperatureUnit
    ): WeatherData {
        val units = when (unit) {
            TemperatureUnit.CELSIUS -> "metric"
            TemperatureUnit.FAHRENHEIT -> "imperial"
        }
        val response = openWeatherApi.getCurrentWeather(latitude, longitude, apiKey, units)
        val temp = response.main?.temp?.toInt()?.toString() ?: "--"
        val suffix = when (unit) {
            TemperatureUnit.CELSIUS -> "C"
            TemperatureUnit.FAHRENHEIT -> "F"
        }
        val desc = response.weather?.firstOrNull()?.description?.replaceFirstChar {
            it.uppercase()
        } ?: "Unknown"
        return WeatherData(temperature = "$temp$suffix", description = desc)
    }

    // Convert WMO weather codes to human-readable description
    private fun wmoCodeToDescription(code: Int): String = when (code) {
        0 -> "Clear sky"
        1 -> "Mainly clear"
        2 -> "Partly cloudy"
        3 -> "Overcast"
        in 45..48 -> "Foggy"
        in 51..55 -> "Drizzle"
        in 56..57 -> "Freezing drizzle"
        in 61..65 -> "Rain"
        in 66..67 -> "Freezing rain"
        in 71..77 -> "Snow"
        in 80..82 -> "Rain showers"
        in 85..86 -> "Snow showers"
        95 -> "Thunderstorm"
        in 96..99 -> "Thunderstorm with hail"
        else -> "Unknown"
    }
}

// Thrown when a paid API returns HTTP 429
class RateLimitException(
    val provider: String,
    val fallbackData: WeatherData
) : Exception("$provider API rate limit exceeded")
