package com.geostampcamera.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

// Holds compass reading
data class CompassData(
    val degrees: Float,
    val direction: String
)

@Singleton
class CompassService @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Observe compass heading as a reactive flow
    fun observeCompass(): Flow<CompassData> = callbackFlow {
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        val gravity = FloatArray(3)
        val geomagnetic = FloatArray(3)
        var hasGravity = false
        var hasMagnetic = false

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, gravity, 0, 3)
                        hasGravity = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, geomagnetic, 0, 3)
                        hasMagnetic = true
                    }
                }

                if (hasGravity && hasMagnetic) {
                    val rotationMatrix = FloatArray(9)
                    val orientation = FloatArray(3)
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geomagnetic)) {
                        SensorManager.getOrientation(rotationMatrix, orientation)
                        val azimuthRadians = orientation[0]
                        var azimuthDegrees = Math.toDegrees(azimuthRadians.toDouble()).toFloat()
                        if (azimuthDegrees < 0) azimuthDegrees += 360f
                        trySend(CompassData(azimuthDegrees, degreesToDirection(azimuthDegrees)))
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                // No-op; accuracy changes are not used
            }
        }

        accelerometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.let {
            sensorManager.registerListener(listener, it, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Convert azimuth degrees to a cardinal direction label
    private fun degreesToDirection(degrees: Float): String = when {
        degrees >= 337.5f || degrees < 22.5f -> "N"
        degrees >= 22.5f && degrees < 67.5f -> "NE"
        degrees >= 67.5f && degrees < 112.5f -> "E"
        degrees >= 112.5f && degrees < 157.5f -> "SE"
        degrees >= 157.5f && degrees < 202.5f -> "S"
        degrees >= 202.5f && degrees < 247.5f -> "SW"
        degrees >= 247.5f && degrees < 292.5f -> "W"
        degrees >= 292.5f && degrees < 337.5f -> "NW"
        else -> "N"
    }
}
