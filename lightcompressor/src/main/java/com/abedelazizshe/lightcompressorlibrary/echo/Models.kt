package com.abedelazizshe.lightcompressorlibrary.echo

import java.io.File

@JvmInline
value class CompressId(val id: String)

data class CompressOutputConfig(val path: String, val fileName: String) {

    val outPath: String
        get() {
            path.checkOrCreateFolderFile()
            val file = File(path, "$fileName.mp4")
            return file.absolutePath
        }
}

data class CompressConfig(
    val filePath: String,
    val id: CompressId = CompressId(filePath),
    val compressEngine: ICompressEngine,
    var disableAudio: Boolean = false,
    val outPathConfig: CompressOutputConfig? = null,
)