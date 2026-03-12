package com.geostampcamera.camera

import android.content.Context
import android.media.MediaActionSound
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.geostampcamera.data.model.AspectRatio
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CameraManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var imageCapture: ImageCapture? = null
    private var currentLensFacing = CameraSelector.LENS_FACING_BACK
    private val shutterSound = MediaActionSound()

    // Initialize and bind the camera to the given preview view
    suspend fun startCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        flashEnabled: Boolean = false,
        aspectRatio: AspectRatio = AspectRatio.RATIO_4_3
    ) {
        currentLensFacing = lensFacing

        val provider = suspendCancellableCoroutine { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                cont.resume(future.get())
            }, ContextCompat.getMainExecutor(context))
        }

        cameraProvider = provider
        provider.unbindAll()

        val cameraXAspectRatio = when (aspectRatio) {
            AspectRatio.RATIO_4_3 -> androidx.camera.core.AspectRatio.RATIO_4_3
            AspectRatio.RATIO_16_9 -> androidx.camera.core.AspectRatio.RATIO_16_9
            AspectRatio.RATIO_1_1 -> androidx.camera.core.AspectRatio.RATIO_4_3
        }

        val preview = Preview.Builder()
            .setTargetAspectRatio(cameraXAspectRatio)
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setTargetAspectRatio(cameraXAspectRatio)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setFlashMode(
                if (flashEnabled) ImageCapture.FLASH_MODE_ON
                else ImageCapture.FLASH_MODE_OFF
            )
            .build()

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        provider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
    }

    // Capture a photo and return the file path
    suspend fun capturePhoto(playSound: Boolean = true): File {
        val capture = imageCapture
            ?: throw IllegalStateException("Camera not initialized")

        val photoFile = File.createTempFile("geostamp_", ".jpg", context.cacheDir)

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        return suspendCancellableCoroutine { cont ->
            capture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        if (playSound) {
                            shutterSound.play(MediaActionSound.SHUTTER_CLICK)
                        }
                        cont.resume(photoFile)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        photoFile.delete()
                        cont.resumeWithException(exception)
                    }
                }
            )
        }
    }

    // Toggle between front and back cameras
    fun getOppositeLensFacing(): Int {
        return if (currentLensFacing == CameraSelector.LENS_FACING_BACK) {
            CameraSelector.LENS_FACING_FRONT
        } else {
            CameraSelector.LENS_FACING_BACK
        }
    }

    // Update the flash mode on the active capture use case
    fun setFlashMode(enabled: Boolean) {
        imageCapture?.flashMode = if (enabled) {
            ImageCapture.FLASH_MODE_ON
        } else {
            ImageCapture.FLASH_MODE_OFF
        }
    }

    // Release camera resources
    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        imageCapture = null
        shutterSound.release()
    }
}
