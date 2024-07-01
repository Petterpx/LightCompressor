package com.abedelazizshe.lightcompressorlibrary.config

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.luck.picture.lib.entity.LocalMedia
import java.io.File
import java.io.FileOutputStream

/**
 * 一些扩展
 * @author petterp
 */

internal fun String.checkOrCreateFolderFile(): Boolean {
    val file = File(this)
    if (!file.exists()) return file.mkdirs()
    return true
}

internal fun File.checkOrCreateFile(): Boolean {
    if (isDirectory) return false
    if (!exists()) return createNewFile()
    return true
}

internal fun String.isContentFile(): Boolean {
    if (this.isEmpty()) return false
    return this.startsWith("content://")
}

internal val LocalMedia.uri: Uri?
    get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
            Uri.parse(path)
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> {
            val file = File(realPath)
            val authority = "${VideoCompressor.context.packageName}.fileprovider"
            FileProvider.getUriForFile(VideoCompressor.context, authority, file)
        }

        else -> {
            File(realPath).toUri()
        }
    }

internal fun String.convertSafePath(context: Context): String {
    if (!this.isContentFile()) return this
    val uri = Uri.parse(this)
    val resolver = context.contentResolver
    val projection = arrayOf(MediaStore.Video.Media.DATA)
    var cursor: Cursor? = null
    try {
        cursor = resolver.query(uri, projection, null, null, null)
        return if (cursor != null) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(columnIndex)
        } else {
            throw Exception()
        }
    } catch (e: Exception) {
        resolver.let {
            val filePath = context.filesDir.absolutePath
            val fileName = "qd-${System.currentTimeMillis()}.mp4"
            val file = File(filePath, fileName)
            resolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buf = ByteArray(4096)
                    var len: Int
                    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(
                        buf,
                        0,
                        len
                    )
                }
            }
            return file.absolutePath
        }
    } finally {
        cursor?.close()
    }
}