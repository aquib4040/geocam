package com.geostampcamera.core.permissions

import android.Manifest
import android.os.Build

// This file serves as a helper for identifying required permissions.
// Permission request logic is handled in the UI layer using ActivityResultLaunchers.

object PermissionHelper {

    // All permissions the app needs at runtime
    fun getRequiredPermissions(): List<String> {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
        }
        return permissions
    }
}
