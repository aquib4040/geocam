package com.geostampcamera.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.geostampcamera.ui.components.SettingsChipRow

import com.geostampcamera.ui.components.SettingsColorRow
import com.geostampcamera.ui.components.SettingsSectionHeader
import com.geostampcamera.ui.components.SettingsSliderRow
import com.geostampcamera.ui.components.SettingsToggleRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val mapKeyTest by viewModel.mapKeyTestState.collectAsStateWithLifecycle()
    val mapplsKeyTest by viewModel.mapplsKeyTestState.collectAsStateWithLifecycle()
    val weatherKeyTest by viewModel.weatherKeyTestState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState()
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // -- Stamp Content Settings --
            SettingsSectionHeader("Stamp Content")
            SettingsToggleRow("Latitude", settings.showLatitude) { viewModel.setShowLatitude(it) }
            SettingsToggleRow("Longitude", settings.showLongitude) { viewModel.setShowLongitude(it) }
            SettingsToggleRow("Address", settings.showAddress) { viewModel.setShowAddress(it) }
            SettingsToggleRow("Date", settings.showDate) { viewModel.setShowDate(it) }
            SettingsToggleRow("Time", settings.showTime) { viewModel.setShowTime(it) }
            SettingsToggleRow("Weather", settings.showWeather) { viewModel.setShowWeather(it) }
            SettingsToggleRow("Altitude", settings.showAltitude) { viewModel.setShowAltitude(it) }
            SettingsToggleRow("Compass", settings.showCompass) { viewModel.setShowCompass(it) }
            SettingsToggleRow("Map Preview", settings.showMapPreview) { viewModel.setShowMapPreview(it) }
            SettingsToggleRow("Custom Text", settings.showCustomText) { viewModel.setShowCustomText(it) }

            if (settings.showCustomText) {
                var textValue by remember(settings.customText) {
                    mutableStateOf(settings.customText)
                }
                androidx.compose.material3.OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it
                        viewModel.setCustomText(it)
                    },
                    label = { Text("Custom stamp text") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Template Settings --
            SettingsSectionHeader("Stamp Template")
            SettingsChipRow(
                options = StampTemplate.entries.toList(),
                selectedOption = settings.stampTemplate,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setStampTemplate(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Map Settings --
            SettingsSectionHeader("Map Settings")

            Text(
                text = "Map Provider",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
            // Use a specific provider chip row that helps visibility
            SettingsChipRow(
                options = MapProvider.entries.toList(),
                selectedOption = settings.mapProvider,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setMapProvider(it) }
            )

            // Added a small hint for MapmyIndia visibility
            if (MapProvider.entries.size > 2) {
                Text(
                    text = "Swipe for more map providers → (MapmyIndia)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }



            Text(
                text = "Map Type",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = MapType.entries.toList(),
                selectedOption = settings.mapType,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setMapType(it) }
            )

            Text(
                text = "Map Size",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = MapSize.entries.toList(),
                selectedOption = settings.mapSize,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setMapSize(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Text Style Settings --
            SettingsSectionHeader("Text Style")

            Text(
                text = "Font Size",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = FontSize.entries.toList(),
                selectedOption = settings.fontSize,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setFontSize(it) }
            )

            SettingsColorRow(
                label = "Font Color",
                colors = StampFontColor.entries.map { it.displayName to it.colorValue },
                selectedColor = settings.fontColor.colorValue,
                onSelected = { index ->
                    viewModel.setFontColor(StampFontColor.entries[index])
                }
            )

            Text(
                text = "Text Alignment",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = StampTextAlignment.entries.toList(),
                selectedOption = settings.textAlignment,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setTextAlignment(it) }
            )

            SettingsSliderRow(
                label = "Background Opacity",
                value = settings.backgroundOpacity,
                valueRange = 0f..1f,
                valueLabel = "${(settings.backgroundOpacity * 100).toInt()}%",
                onValueChange = { viewModel.setBackgroundOpacity(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Camera Settings --
            SettingsSectionHeader("Camera")
            SettingsToggleRow("Flash", settings.flashEnabled) { viewModel.setFlashEnabled(it) }
            SettingsToggleRow("Grid Overlay", settings.gridEnabled) { viewModel.setGridEnabled(it) }
            SettingsToggleRow("Mirror Front Camera", settings.mirrorMode) { viewModel.setMirrorMode(it) }
            SettingsToggleRow("Shutter Sound", settings.captureSound) { viewModel.setCaptureSound(it) }

            Text(
                text = "Self Timer",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = TimerOption.entries.toList(),
                selectedOption = settings.timerOption,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setTimerOption(it) }
            )

            Text(
                text = "Aspect Ratio",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = AspectRatio.entries.toList(),
                selectedOption = settings.aspectRatio,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setAspectRatio(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Location Settings --
            SettingsSectionHeader("Location")
            SettingsToggleRow("Auto GPS", settings.useAutoGps) { viewModel.setUseAutoGps(it) }

            if (!settings.useAutoGps) {
                var latStr by remember(settings.manualLatitude) {
                    mutableStateOf(settings.manualLatitude.toString())
                }
                var lngStr by remember(settings.manualLongitude) {
                    mutableStateOf(settings.manualLongitude.toString())
                }
                androidx.compose.material3.OutlinedTextField(
                    value = latStr,
                    onValueChange = {
                        latStr = it
                        it.toDoubleOrNull()?.let { d -> viewModel.setManualLatitude(d) }
                    },
                    label = { Text("Latitude") },
                    singleLine = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                androidx.compose.material3.OutlinedTextField(
                    value = lngStr,
                    onValueChange = {
                        lngStr = it
                        it.toDoubleOrNull()?.let { d -> viewModel.setManualLongitude(d) }
                    },
                    label = { Text("Longitude") },
                    singleLine = true,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Weather Settings --
            SettingsSectionHeader("Weather")

            Text(
                text = "Provider",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = WeatherProvider.entries.toList(),
                selectedOption = settings.weatherProvider,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setWeatherProvider(it) }
            )


            Text(
                text = "Temperature Unit",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = TemperatureUnit.entries.toList(),
                selectedOption = settings.temperatureUnit,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setTemperatureUnit(it) }
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- File Save Settings --
            SettingsSectionHeader("File Save")

            Text(
                text = "Image Quality",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = ImageQuality.entries.toList(),
                selectedOption = settings.imageQuality,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setImageQuality(it) }
            )

            Text(
                text = "Naming Style",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
            )
            SettingsChipRow(
                options = NamingStyle.entries.toList(),
                selectedOption = settings.namingStyle,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setNamingStyle(it) }
            )

            var folderValue by remember(settings.saveFolder) {
                mutableStateOf(settings.saveFolder)
            }
            androidx.compose.material3.OutlinedTextField(
                value = folderValue,
                onValueChange = {
                    folderValue = it
                    viewModel.setSaveFolder(it)
                },
                label = { Text("Save Folder (inside Pictures)") },
                placeholder = { Text("GeoStampCamera") },
                singleLine = true,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- API Keys Section (always visible) --
            SettingsSectionHeader("API Keys")
            Text(
                text = "Add API keys to unlock premium map and weather providers. Leave blank to use free defaults.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // Google Maps API Key
            var gmApiKey by remember(settings.googleMapsApiKey) {
                mutableStateOf(settings.googleMapsApiKey)
            }
            androidx.compose.material3.OutlinedTextField(
                value = gmApiKey,
                onValueChange = {
                    gmApiKey = it
                    viewModel.setGoogleMapsApiKey(it)
                    viewModel.clearMapKeyTestState()
                },
                label = { Text("Google Maps API Key") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.testGoogleMapsKey(gmApiKey) },
                    enabled = gmApiKey.isNotBlank() && !mapKeyTest.isTesting
                ) {
                    if (mapKeyTest.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Test Key")
                }
                mapKeyTest.resultMessage?.let { msg ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (mapKeyTest.isSuccess) "✓ Valid" else msg,
                        color = if (mapKeyTest.isSuccess)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // MapmyIndia / Mappls API Key
            var mmApiKey by remember(settings.mapplsApiKey) {
                mutableStateOf(settings.mapplsApiKey)
            }
            androidx.compose.material3.OutlinedTextField(
                value = mmApiKey,
                onValueChange = {
                    mmApiKey = it
                    viewModel.setMapplsApiKey(it)
                    viewModel.clearMapplsKeyTestState()
                },
                label = { Text("MapmyIndia / Mappls API Key") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.testMapplsKey(mmApiKey) },
                    enabled = mmApiKey.isNotBlank() && !mapplsKeyTest.isTesting
                ) {
                    if (mapplsKeyTest.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Test Key")
                }
                mapplsKeyTest.resultMessage?.let { msg ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (mapplsKeyTest.isSuccess) "✓ Valid" else msg,
                        color = if (mapplsKeyTest.isSuccess)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // OpenWeather API Key
            var owApiKey by remember(settings.openWeatherApiKey) {
                mutableStateOf(settings.openWeatherApiKey)
            }
            androidx.compose.material3.OutlinedTextField(
                value = owApiKey,
                onValueChange = {
                    owApiKey = it
                    viewModel.setOpenWeatherApiKey(it)
                    viewModel.clearWeatherKeyTestState()
                },
                label = { Text("OpenWeather API Key") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
            Row(
                modifier = Modifier.padding(bottom = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Button(
                    onClick = { viewModel.testOpenWeatherKey(owApiKey) },
                    enabled = owApiKey.isNotBlank() && !weatherKeyTest.isTesting
                ) {
                    if (weatherKeyTest.isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Test Key")
                }
                weatherKeyTest.resultMessage?.let { msg ->
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (weatherKeyTest.isSuccess) "✓ Valid" else msg,
                        color = if (weatherKeyTest.isSuccess)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // -- Theme Settings --
            SettingsSectionHeader("App Theme")
            SettingsChipRow(
                options = AppTheme.entries.toList(),
                selectedOption = settings.appTheme,
                labelProvider = { it.displayName },
                onSelected = { viewModel.setAppTheme(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
