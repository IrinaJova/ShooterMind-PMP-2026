package com.shootermind.app.core.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Shared helpers for camera and gallery image handling.
 */
object FileUtils {

    /**
     * Creates a temp file in cacheDir for the camera to write into,
     * and returns a content:// URI that can be passed to TakePicture().
     */
    fun createCameraImageUri(context: Context): Pair<Uri, File> {
        val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
        val file = File(dir, "${System.currentTimeMillis()}.jpg")
        val uri  = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return uri to file
    }

    /**
     * Copies a content URI (gallery pick) to the app's internal storage
     * under [subfolder] and returns the absolute path of the copy.
     * Returns null if copying fails.
     */
    fun copyUriToInternal(context: Context, uri: Uri, subfolder: String): String? =
        try {
            val dir  = File(context.filesDir, subfolder).also { it.mkdirs() }
            val dest = File(dir, "${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            dest.absolutePath
        } catch (_: Exception) { null }
}
