package com.abedelazizshe.lightcompressorlibrary

import android.annotation.SuppressLint
import android.content.Context
import com.abedelazizshe.lightcompressorlibrary.compressor.Compressor
import com.abedelazizshe.lightcompressorlibrary.echo.CompressLister
import com.abedelazizshe.lightcompressorlibrary.echo.CompressListerImpl
import com.abedelazizshe.lightcompressorlibrary.echo.CompressConfig
import com.abedelazizshe.lightcompressorlibrary.echo.checkOrCreateFolderFile
import com.abedelazizshe.lightcompressorlibrary.echo.convertSafePath
import com.abedelazizshe.lightcompressorlibrary.video.CompressResult
import com.luck.picture.lib.entity.LocalMedia
import java.io.File


/**
 * Echo视频压缩
 * @author petterp
 */
@SuppressLint("StaticFieldLeak")
object VideoCompressor {

    internal lateinit var context: Context

    fun init(context: Context) {
        this.context = context
    }

    private fun defaultOutputPath(): String {
        val fileParent = context.filesDir.absolutePath
        fileParent.checkOrCreateFolderFile()
        val file = File(fileParent, "compress-${System.currentTimeMillis()}.mp4")
        return file.absolutePath
    }

    suspend fun start(
        models: List<CompressConfig>,
        enableDebug: Boolean,
        listener: (CompressLister.() -> Unit)?
    ): List<CompressResult> {
        return models.map {
            start(it, enableDebug, listener)
        }
    }

    suspend fun start(
        model: CompressConfig,
        enableDebug: Boolean,
        listener: (CompressLister.() -> Unit)? = null
    ): CompressResult {
        val id = model.id
        val startTime = System.currentTimeMillis()
        val listerImp = if (listener != null) CompressListerImpl().also(listener) else null
        listerImp?.startListener?.invoke(id)
        // 获取绝对路径,如果没有,那就先复制到缓存文件夹,后面会自动删除
        Compressor.log.d("start compress, id:${model.id}")
        var srcPath = model.filePath
        return runCatching {
            srcPath = srcPath.convertSafePath(context)
            Compressor.compressVideo(
                context,
                srcPath,
                model.outPathConfig?.outPath ?: defaultOutputPath(),
                model
            ).copy(time = System.currentTimeMillis() - startTime)
        }.getOrElse {
            val endTime = System.currentTimeMillis() - startTime
            CompressResult(id, false, it.message, 0, time = endTime)
        }.apply {
            val file = File(this.path ?: "")
            // 自动删除临时缓存的压缩文件，这本就是兜底的逻辑
            // 如果未来想省去一步IO操作
            checkOrDeleteCompressTempFile(model.filePath, srcPath)
            if (success && file.length() > 1) {
                outputLog(enableDebug, model.filePath, this)
                listerImp?.successListener?.invoke(id, size, path, time)
            } else {
                Compressor.log.e("start compress fail, id:${model.id},message:$failureMessage")
                listerImp?.failureListener?.invoke(id, failureMessage ?: "compress failed")
            }
        }
    }

    private fun outputLog(
        isEnable: Boolean,
        originPath: String,
        result: CompressResult,
    ) {
        if (!isEnable) return
        val originMedia = LocalMedia.generateLocalMedia(context, originPath)
        val compressMedia = LocalMedia.generateLocalMedia(context, result.path)
        Compressor.log.d(
            "compress End, id:${result.id}  \n" +
                    "- time: [${result.time}]\n" +
                    "- size: [${originMedia.size}] ->  [${compressMedia.size}] \n" +
                    "- w,h:  [${originMedia.width},${originMedia.height}] -> [${compressMedia.width},${compressMedia.height}]" +
                    "- srcPath: ${originPath}\n" +
                    "- compressPath: ${result.path}\n" +
                    "\n"
        )
    }

    private fun checkOrDeleteCompressTempFile(originPath: String, tempPath: String): Boolean {
        kotlin.runCatching {
            if (originPath == tempPath) return false
            val tempFile = File(tempPath)
            tempFile.delete()
        }
        return true
    }
}