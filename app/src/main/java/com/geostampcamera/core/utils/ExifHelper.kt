package com.geostampcamera.core.utils

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import com.geostampcamera.data.model.PhotoMetadata
import java.io.IOException

object ExifHelper {

    // Write GPS and timestamp EXIF metadata to a saved image
    fun writeExifData(context: Context, uri: Uri, metadata: PhotoMetadata) {
        try {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "rw") ?: return
            descriptor.use { fd ->
                val exif = ExifInterface(fd.fileDescriptor)

                if (metadata.latitude != 0.0 && metadata.longitude != 0.0) {
                    exif.setLatLong(metadata.latitude, metadata.longitude)
                }

                if (metadata.altitude != 0.0) {
                    exif.setAltitude(metadata.altitude)
                }

                exif.setAttribute(
                    ExifInterface.TAG_DATETIME,
                    "${metadata.date} ${metadata.time}"
                )

                exif.setAttribute(
                    ExifInterface.TAG_USER_COMMENT,
                    "Address: ${metadata.address} | Weather: ${metadata.temperature} ${metadata.weatherDescription} | Compass: ${metadata.compassDirection}"
                )

                exif.saveAttributes()
            }
        } catch (e: IOException) {
            // Silently fail; EXIF is supplementary data
        }
    }
}
