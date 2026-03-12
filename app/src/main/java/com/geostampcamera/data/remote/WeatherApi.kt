package com.geostampcamera.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

// OpenWeather API response models
data class OpenWeatherResponse(
    val main: MainData?,
    val weather: List<WeatherItem>?
)

data class MainData(
    val temp: Double
)

data class WeatherItem(
    val description: String,
    val icon: String
)

// Open-Meteo API response models (free, no key required)
data class OpenMeteoResponse(
    val current_weather: CurrentWeather?
)

data class CurrentWeather(
    val temperature: Double,
    val weathercode: Int,
    val windspeed: Double
)

// OpenWeather API interface
interface OpenWeatherApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherResponse
}

// Open-Meteo API interface (free, no key)
interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getCurrentWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") currentWeather: Boolean = true
    ): OpenMeteoResponse
}
