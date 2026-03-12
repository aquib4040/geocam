package com.geostampcamera.data.repository

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import com.geostampcamera.core.utils.ExifHelper
import com.geostampcamera.data.model.AppSettings
import com.geostampcamera.data.model.ImageQuality
import com.geostampcamera.data.model.NamingStyle
import com.geostampcamera.data.model.PhotoMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Save a stamped bitmap to the device gallery and write EXIF metadata
    suspend fun saveStampedPhoto(
        bitmap: Bitmap,
        metadata: PhotoMetadata,
        settings: AppSettings
    ): String? = withContext(Dispatchers.IO) {
        val fileName = generateFileName(metadata, settings)
        val folderName = settings.saveFolder.ifEmpty { "GeoStampCamera" }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(
                MediaStore.Images.Media.RELATIVE_PATH,
                "${Environment.DIRECTORY_PICTURES}${File.separator}$folderName"
            )
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val resolver = context.contentResolver
        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: return@withContext null

        try {
            resolver.openOutputStream(imageUri)?.use { outputStream ->
                val quality = when (settings.imageQuality) {
                    ImageQuality.LOW -> 50
                    ImageQuality.MEDIUM -> 75
                    ImageQuality.HIGH -> 90
                    ImageQuality.MAXIMUM -> 100
                }
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(imageUri, contentValues, null, null)

            // Write EXIF data to the saved file
            ExifHelper.writeExifData(context, imageUri, metadata)

            return@withContext imageUri.toString()
        } catch (e: Exception) {
            resolver.delete(imageUri, null, null)
            return@withContext null
        }
    }

    // Build a filename based on the user's naming style preference
    private fun generateFileName(
        metadata: PhotoMetadata,
        settings: AppSettings
    ): String {
        val dateFormat = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US)
        val dateStr = dateFormat.format(Date())

        return when (settings.namingStyle) {
            NamingStyle.CITY_DATE_TIME -> {
                val city = metadata.city
                    .replace(" ", "_")
                    .replace(Regex("[^a-zA-Z0-9_]"), "")
                    .ifEmpty { "Unknown" }
                "${city}_$dateStr.jpg"
            }
            NamingStyle.DATE_TIME -> {
                "IMG_$dateStr.jpg"
            }
            NamingStyle.SEQUENTIAL -> {
                val timestamp = System.currentTimeMillis()
                "GeoStamp_$timestamp.jpg"
            }
        }
    }
}
