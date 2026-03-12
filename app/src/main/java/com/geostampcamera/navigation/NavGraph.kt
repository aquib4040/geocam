package com.geostampcamera.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.geostampcamera.ui.camera.CameraScreen
import com.geostampcamera.ui.preview.PhotoPreviewScreen
import com.geostampcamera.ui.settings.SettingsScreen
import com.geostampcamera.ui.template.TemplateSelectorScreen

object Routes {
    const val CAMERA = "camera"
    const val PREVIEW = "preview/{photoPath}"
    const val SETTINGS = "settings"
    const val TEMPLATE_SELECTOR = "template_selector"

    fun previewRoute(photoPath: String): String =
        "preview/${java.net.URLEncoder.encode(photoPath, "UTF-8")}"
}

@Composable
fun GeoStampNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.CAMERA
    ) {
        composable(Routes.CAMERA) {
            CameraScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToTemplates = { navController.navigate(Routes.TEMPLATE_SELECTOR) },
                onPhotoTaken = { path -> navController.navigate(Routes.previewRoute(path)) }
            )
        }

        composable(
            route = Routes.PREVIEW,
            arguments = listOf(navArgument("photoPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val photoPath = java.net.URLDecoder.decode(
                backStackEntry.arguments?.getString("photoPath") ?: "",
                "UTF-8"
            )
            PhotoPreviewScreen(
                photoPath = photoPath,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.popBackStack(Routes.CAMERA, inclusive = false)
                }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TEMPLATE_SELECTOR) {
            TemplateSelectorScreen(onBack = { navController.popBackStack() })
        }
    }
}
