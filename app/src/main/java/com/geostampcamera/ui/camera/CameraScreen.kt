package com.geostampcamera.ui.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.geostampcamera.core.permissions.PermissionHelper

@Composable
fun CameraScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToTemplates: () -> Unit,
    onPhotoTaken: (String) -> Unit,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val compass by viewModel.compassData.collectAsStateWithLifecycle()

    // Permission handling
    var permissionsGranted by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissionsGranted = permissions.values.all { it }
    }

    LaunchedEffect(Unit) {
        val requiredPermissions = PermissionHelper.getRequiredPermissions()
        val allGranted = requiredPermissions.all { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            permissionsGranted = true
        } else {
            permissionLauncher.launch(requiredPermissions.toTypedArray())
        }
    }

    // Navigate to preview when photo is taken
    LaunchedEffect(uiState.lastPhotoPath) {
        uiState.lastPhotoPath?.let { path ->
            onPhotoTaken(path)
        }
    }

    if (!permissionsGranted) {
        PermissionDeniedContent()
        return
    }

    // Camera preview setup
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(uiState.lensFacing, settings.aspectRatio) {
        viewModel.cameraManagerInstance.startCamera(
            previewView = previewView,
            lifecycleOwner = lifecycleOwner,
            lensFacing = uiState.lensFacing,
            flashEnabled = settings.flashEnabled,
            aspectRatio = settings.aspectRatio
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Grid overlay
        if (settings.gridEnabled) {
            GridOverlay(modifier = Modifier.fillMaxSize())
        }

        // Timer countdown display
        if (uiState.timerCountdown > 0) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${uiState.timerCountdown}",
                    color = Color.White,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Top controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash toggle
            IconButton(onClick = { viewModel.toggleFlash() }) {
                Icon(
                    imageVector = if (settings.flashEnabled) Icons.Filled.FlashOn
                    else Icons.Filled.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }

            // Compass indicator
            Text(
                text = "${compass.direction} ${compass.degrees.toInt()}",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier
                    .background(Color(0x80000000), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )

            // Grid toggle
            IconButton(onClick = { viewModel.toggleGrid() }) {
                Icon(
                    imageVector = if (settings.gridEnabled) Icons.Filled.GridOn
                    else Icons.Filled.GridOff,
                    contentDescription = "Grid",
                    tint = Color.White
                )
            }

            // Settings button
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = Color.White
                )
            }
        }

        // Bottom controls bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 24.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Template selector
            IconButton(
                onClick = onNavigateToTemplates,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Style,
                    contentDescription = "Templates",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Capture button
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.9f))
                    .clickable(enabled = !uiState.isCapturing) {
                        viewModel.capturePhoto()
                    },
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isCapturing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }

            // Camera switch
            IconButton(
                onClick = { viewModel.toggleCamera() },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Cameraswitch,
                    contentDescription = "Switch Camera",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Error snackbar
        uiState.errorMessage?.let { error ->
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(error)
            }
        }
    }

    // Rate limit popup dialog
    uiState.rateLimitMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissRateLimitMessage() },
            title = { Text("API Rate Limit Exceeded") },
            text = {
                Text(
                    text = message + "\n\nYour photo was saved using the free provider.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissRateLimitMessage() }) {
                    Text("OK")
                }
            }
        )
    }
}

// Rule of thirds grid overlay
@Composable
private fun GridOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeColor = Color.White.copy(alpha = 0.5f)
        val strokeWidth = 1f

        // Vertical lines
        val thirdWidth = size.width / 3f
        drawLine(strokeColor, Offset(thirdWidth, 0f), Offset(thirdWidth, size.height), strokeWidth)
        drawLine(strokeColor, Offset(thirdWidth * 2, 0f), Offset(thirdWidth * 2, size.height), strokeWidth)

        // Horizontal lines
        val thirdHeight = size.height / 3f
        drawLine(strokeColor, Offset(0f, thirdHeight), Offset(size.width, thirdHeight), strokeWidth)
        drawLine(strokeColor, Offset(0f, thirdHeight * 2), Offset(size.width, thirdHeight * 2), strokeWidth)
    }
}

// Shown when permissions are not granted
@Composable
private fun PermissionDeniedContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Camera and Location permissions are required",
                color = Color.White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(32.dp)
            )
            Text(
                text = "Please grant permissions in app settings",
                color = Color.Gray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
