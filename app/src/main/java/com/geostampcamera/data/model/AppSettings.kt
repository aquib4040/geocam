package com.geostampcamera.data.model

// Stamp template styles
enum class StampTemplate(val displayName: String) {
    CLASSIC("Classic"),
    MODERN("Modern"),
    MINIMAL("Minimal"),
    SURVEY("Survey"),
    TRAVEL("Travel"),
    PROFESSIONAL("Professional")
}

// Map provider options
enum class MapProvider(val displayName: String) {
    OPENSTREETMAP("OpenStreetMap (Free)"),
    GOOGLE_MAPS("Google Maps (API Key Required)")
}

// Map display type
enum class MapType(val displayName: String) {
    NORMAL("Normal"),
    SATELLITE("Satellite"),
    TERRAIN("Terrain"),
    HYBRID("Hybrid")
}

// Map size in stamp
enum class MapSize(val displayName: String, val widthDp: Int, val heightDp: Int) {
    SMALL("Small", 150, 100),
    MEDIUM("Medium", 250, 160),
    LARGE("Large", 350, 220)
}

// Stamp text font size
enum class FontSize(val displayName: String, val sp: Float) {
    SMALL("Small", 10f),
    MEDIUM("Medium", 14f),
    LARGE("Large", 18f),
    EXTRA_LARGE("Extra Large", 22f)
}

// Stamp text font color
enum class StampFontColor(val displayName: String, val colorValue: Long) {
    WHITE("White", 0xFFFFFFFF),
    BLACK("Black", 0xFF000000),
    YELLOW("Yellow", 0xFFFFEB3B),
    RED("Red", 0xFFF44336),
    GREEN("Green", 0xFF4CAF50),
    BLUE("Blue", 0xFF2196F3)
}

// Stamp text alignment
enum class StampTextAlignment(val displayName: String) {
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right")
}

// Camera aspect ratio
enum class AspectRatio(val displayName: String) {
    RATIO_4_3("4:3"),
    RATIO_16_9("16:9"),
    RATIO_1_1("1:1")
}

// Temperature unit
enum class TemperatureUnit(val displayName: String) {
    CELSIUS("Celsius"),
    FAHRENHEIT("Fahrenheit")
}

// Weather data provider
enum class WeatherProvider(val displayName: String) {
    OPEN_METEO("Open-Meteo (Free)"),
    OPENWEATHER("OpenWeather (API Key Required)")
}

// Saved image quality
enum class ImageQuality(val displayName: String, val quality: Int) {
    LOW("Low", 50),
    MEDIUM("Medium", 75),
    HIGH("High", 90),
    MAXIMUM("Maximum", 100)
}

// Photo file naming style
enum class NamingStyle(val displayName: String) {
    CITY_DATE_TIME("City_Date_Time"),
    DATE_TIME("Date_Time"),
    SEQUENTIAL("Sequential")
}

// App theme mode
enum class AppTheme(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System")
}

// Timer duration options in seconds
enum class TimerOption(val displayName: String, val seconds: Int) {
    OFF("Off", 0),
    THREE("3s", 3),
    FIVE("5s", 5),
    TEN("10s", 10)
}

// All application settings stored in DataStore
data class AppSettings(
    // Stamp content toggles
    val showLatitude: Boolean = true,
    val showLongitude: Boolean = true,
    val showAddress: Boolean = true,
    val showDate: Boolean = true,
    val showTime: Boolean = true,
    val showWeather: Boolean = true,
    val showAltitude: Boolean = true,
    val showCompass: Boolean = true,
    val showMapPreview: Boolean = true,
    val showCustomText: Boolean = false,
    val customText: String = "",

    // Template
    val stampTemplate: StampTemplate = StampTemplate.CLASSIC,

    // Map
    val mapProvider: MapProvider = MapProvider.OPENSTREETMAP,
    val mapType: MapType = MapType.NORMAL,
    val mapSize: MapSize = MapSize.MEDIUM,
    val googleMapsApiKey: String = "",

    // Text style
    val fontSize: FontSize = FontSize.MEDIUM,
    val fontColor: StampFontColor = StampFontColor.WHITE,
    val textAlignment: StampTextAlignment = StampTextAlignment.LEFT,
    val backgroundOpacity: Float = 0.7f,

    // Camera
    val flashEnabled: Boolean = false,
    val gridEnabled: Boolean = false,
    val timerOption: TimerOption = TimerOption.OFF,
    val aspectRatio: AspectRatio = AspectRatio.RATIO_4_3,
    val mirrorMode: Boolean = false,
    val captureSound: Boolean = true,

    // Location
    val useAutoGps: Boolean = true,
    val manualLatitude: Double = 0.0,
    val manualLongitude: Double = 0.0,

    // Weather
    val weatherProvider: WeatherProvider = WeatherProvider.OPEN_METEO,
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val openWeatherApiKey: String = "",

    // File save
    val imageQuality: ImageQuality = ImageQuality.HIGH,
    val namingStyle: NamingStyle = NamingStyle.CITY_DATE_TIME,
    val saveFolder: String = "",

    // Theme
    val appTheme: AppTheme = AppTheme.SYSTEM
)
