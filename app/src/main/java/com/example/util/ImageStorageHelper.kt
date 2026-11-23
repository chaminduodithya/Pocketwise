package com.example.util

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import java.util.UUID

object ImageStorageHelper {
    const val GOAL_IMAGES_DIR = "goal_images"
    const val RECEIPT_IMAGES_DIR = "receipt_images"

    private val allowedExtensions = setOf("jpg", "jpeg", "png", "webp")

    /**
     * Copies a picked [sourceUri] into app-private internal storage ([Context.getFilesDir]) so the
     * image remains readable across app restarts, process death, and cache eviction without needing
     * storage permissions or relying on temporary Photo Picker URI grants.
     */
    suspend fun persistPickedImage(
        context: Context,
        sourceUri: Uri,
        subdirectory: String
    ): String? = withContext(Dispatchers.IO) {
        val targetDir = File(context.filesDir, subdirectory)
        if (isUriInsideDirectory(sourceUri, targetDir)) {
            val existingFile = sourceUri.path?.let(::File)
            if (existingFile != null && existingFile.exists() && existingFile.length() > 0L) {
                return@withContext sourceUri.toString()
            }
        }

        runCatching {
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                return@withContext null
            }
            val mimeType = runCatching { context.contentResolver.getType(sourceUri) }.getOrNull()
            val ext = resolveExtension(mimeType, sourceUri.lastPathSegment)
            val prefix = if (subdirectory == RECEIPT_IMAGES_DIR) "receipt" else "goal"
            val destFile = File(targetDir, "${prefix}_${UUID.randomUUID()}.$ext")

            val copiedBytes = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: 0L

            if (copiedBytes <= 0L || !destFile.exists() || destFile.length() <= 0L) {
                destFile.delete()
                null
            } else {
                Uri.fromFile(destFile).toString()
            }
        }.getOrNull()
    }

    fun isOwnedLocalImage(context: Context, uriString: String?): Boolean {
        if (uriString.isNullOrBlank()) return false
        val uri = runCatching { Uri.parse(uriString) }.getOrNull() ?: return false
        val goalDir = File(context.filesDir, GOAL_IMAGES_DIR)
        val receiptDir = File(context.filesDir, RECEIPT_IMAGES_DIR)
        return isUriInsideDirectory(uri, goalDir) || isUriInsideDirectory(uri, receiptDir)
    }

    fun deleteOwnedImage(context: Context, uriString: String?) {
        if (!isOwnedLocalImage(context, uriString)) return
        val path = runCatching { Uri.parse(uriString).path }.getOrNull() ?: return
        runCatching {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

    internal fun resolveExtension(mimeType: String?, pathHint: String?): String {
        val fromMime = if (!mimeType.isNullOrBlank()) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.lowercase(Locale.US)
        } else {
            null
        }
        if (fromMime != null && fromMime in allowedExtensions) {
            return fromMime
        }
        val fromPath = pathHint
            ?.substringAfterLast('.', "")
            ?.lowercase(Locale.US)
        return if (fromPath != null && fromPath in allowedExtensions) fromPath else "jpg"
    }

    internal fun isFileInsideDirectory(file: File, directory: File): Boolean {
        return runCatching {
            val dirCanonical = directory.canonicalPath.trimEnd(File.separatorChar) + File.separatorChar
            file.canonicalPath.startsWith(dirCanonical)
        }.getOrDefault(false)
    }

    private fun isUriInsideDirectory(uri: Uri, directory: File): Boolean {
        if (!"file".equals(uri.scheme, ignoreCase = true)) return false
        val path = uri.path ?: return false
        return isFileInsideDirectory(File(path), directory)
    }
}
