package com.geostampcamera.stamp

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import com.geostampcamera.data.model.AppSettings
import com.geostampcamera.data.model.FontSize
import com.geostampcamera.data.model.PhotoMetadata
import com.geostampcamera.data.model.StampTextAlignment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StampRenderer @Inject constructor(
    private val templateEngine: TemplateEngine
) {

    // Render the stamp overlay onto a copy of the original photo bitmap
    fun renderStamp(
        originalBitmap: Bitmap,
        metadata: PhotoMetadata,
        settings: AppSettings
    ): Bitmap {
        val width = originalBitmap.width
        val height = originalBitmap.height
        val result = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val template = templateEngine.getTemplate(settings)
        val lines = buildStampLines(metadata, settings)

        if (lines.isEmpty() && metadata.mapSnapshot == null) return result

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = settings.fontColor.colorValue.toInt()
            textSize = calculateTextSize(settings.fontSize, width)
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = when (settings.textAlignment) {
                StampTextAlignment.LEFT -> Paint.Align.LEFT
                StampTextAlignment.CENTER -> Paint.Align.CENTER
                StampTextAlignment.RIGHT -> Paint.Align.RIGHT
            }
        }

        val lineHeight = textPaint.textSize * 1.4f
        val padding = width * 0.03f
        val mapBitmap = metadata.mapSnapshot

        // Calculate total stamp height
        val textBlockHeight = lines.size * lineHeight + padding * 2
        val mapHeight = if (mapBitmap != null && settings.showMapPreview) {
            val scaledMapHeight = (mapBitmap.height.toFloat() / mapBitmap.width * (width * 0.35f))
            scaledMapHeight + padding
        } else 0f
        val totalStampHeight = textBlockHeight + mapHeight + padding

        // Draw stamp background
        val bgAlpha = (settings.backgroundOpacity * 255).toInt().coerceIn(0, 255)

        val stampTop = when (template.position) {
            StampPosition.BOTTOM -> height - totalStampHeight
            StampPosition.TOP -> 0f
        }

        val bgPaint = Paint().apply {
            color = Color.argb(bgAlpha, 0, 0, 0)
            style = Paint.Style.FILL
        }

        val bgRect = RectF(0f, stampTop, width.toFloat(), stampTop + totalStampHeight)
        canvas.drawRect(bgRect, bgPaint)

        // Draw map snapshot if enabled
        var mapBottom = stampTop + padding
        if (mapBitmap != null && settings.showMapPreview) {
            val mapWidth = (width * 0.35f).toInt()
            val scaledMapHeight = (mapBitmap.height.toFloat() / mapBitmap.width * mapWidth).toInt()

            val mapLeft = when (template.mapAlignment) {
                MapAlignment.LEFT -> padding.toInt()
                MapAlignment.RIGHT -> (width - mapWidth - padding).toInt()
                MapAlignment.CENTER -> ((width - mapWidth) / 2)
            }

            val mapRect = Rect(
                mapLeft,
                (stampTop + padding).toInt(),
                mapLeft + mapWidth,
                (stampTop + padding + scaledMapHeight).toInt()
            )

            // Draw a border around the map
            val borderPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            canvas.drawBitmap(mapBitmap, null, mapRect, null)
            canvas.drawRect(RectF(mapRect), borderPaint)
            mapBottom = stampTop + padding + scaledMapHeight + padding
        }

        // Draw text lines
        val textX = when (settings.textAlignment) {
            StampTextAlignment.LEFT -> padding
            StampTextAlignment.CENTER -> width / 2f
            StampTextAlignment.RIGHT -> width - padding
        }

        var currentY = mapBottom + lineHeight
        for (line in lines) {
            canvas.drawText(line, textX, currentY, textPaint)
            currentY += lineHeight
        }

        return result
    }

    // Build the list of text lines to display based on user settings
    private fun buildStampLines(
        metadata: PhotoMetadata,
        settings: AppSettings
    ): List<String> {
        val lines = mutableListOf<String>()

        if (settings.showLatitude) {
            lines.add("Lat: %.6f".format(metadata.latitude))
        }
        if (settings.showLongitude) {
            lines.add("Lng: %.6f".format(metadata.longitude))
        }
        if (settings.showAddress && metadata.address.isNotBlank()) {
            lines.add(metadata.address)
        }
        if (settings.showDate && metadata.date.isNotBlank()) {
            lines.add("Date: ${metadata.date}")
        }
        if (settings.showTime && metadata.time.isNotBlank()) {
            lines.add("Time: ${metadata.time}")
        }
        if (settings.showWeather && metadata.temperature.isNotBlank()) {
            lines.add("${metadata.temperature} | ${metadata.weatherDescription}")
        }
        if (settings.showAltitude) {
            lines.add("Alt: %.1f m".format(metadata.altitude))
        }
        if (settings.showCompass && metadata.compassDirection.isNotBlank()) {
            lines.add("Compass: ${metadata.compassDirection} (%.0f)".format(metadata.compassDegrees))
        }
        if (settings.showCustomText && settings.customText.isNotBlank()) {
            lines.add(settings.customText)
        }

        return lines
    }

    // Scale font size based on image width for consistent appearance across resolutions
    private fun calculateTextSize(fontSize: FontSize, imageWidth: Int): Float {
        val baseSize = when (fontSize) {
            FontSize.SMALL -> 0.018f
            FontSize.MEDIUM -> 0.024f
            FontSize.LARGE -> 0.032f
            FontSize.EXTRA_LARGE -> 0.040f
        }
        return imageWidth * baseSize
    }
}

// Stamp region position on the image
enum class StampPosition {
    TOP, BOTTOM
}

// Map snapshot alignment within the stamp region
enum class MapAlignment {
    LEFT, CENTER, RIGHT
}
