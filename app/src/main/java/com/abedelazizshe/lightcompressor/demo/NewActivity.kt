package com.abedelazizshe.lightcompressor.demo

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.abedelazizshe.lightcompressor.databinding.ActivityNewBinding
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.abedelazizshe.lightcompressorlibrary.config.CompressConfig
import com.abedelazizshe.lightcompressorlibrary.config.VideoDefaultCompressEngine
import com.bumptech.glide.Glide
import com.luck.picture.lib.entity.LocalMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 *
 * @author petterp
 */
class NewActivity : AppCompatActivity() {
    private lateinit var bing: ActivityNewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VideoCompressor.init(applicationContext)
        bing = ActivityNewBinding.inflate(layoutInflater)
        setContentView(bing.root)
        var newPath = ""
        bing.btnSelect.setOnClickListener {
            lifecycleScope.launch {
                val result =
                    PictureSelectorHelper.launchSingleVideo(this@NewActivity) ?: return@launch
                Glide.with(bing.image).load(result.path).centerCrop().into(bing.image)
                val builder = StringBuilder()
                builder.append("原始路径：${result.path}\n")
                builder.append("可用路径：${result.availablePath}\n")
                builder.append("分辨率：${result.width}*${result.height}\n")
                builder.append("视频大小：${result.size.M}\n")
                builder.append("id：${result.id}\n")
                builder.append("mimeType：${result.mimeType}\n")
                builder.append("duration：${result.duration}\n")
                builder.append("fileName：${result.fileName}\n")
                builder.append("parentName：${result.parentFolderName}\n")
                newPath = result.path
                bing.tvInfo.text = builder.toString()
            }
        }
        bing.btnCompress.setOnClickListener {
            bing.progress.progress = 0
            lifecycleScope.launch(Dispatchers.IO) {
                val config = CompressConfig(newPath, compressEngine = VideoDefaultCompressEngine)
                val path = VideoCompressor.start(config, enableDebug = true).path
                withContext(Dispatchers.Main) {
                    val result = LocalMedia.generateLocalMedia(this@NewActivity, path)
                    val builder = StringBuilder()
                    builder.append("原始路径：${result.path}\n")
                    builder.append("可用路径：${result.availablePath}\n")
                    builder.append("分辨率：${result.width}*${result.height}\n")
                    builder.append("视频大小：${result.size.M}\n")
                    builder.append("id：${result.id}\n")
                    builder.append("mimeType：${result.mimeType}\n")
                    builder.append("duration：${result.duration}\n")
                    builder.append("fileName：${result.fileName}\n")
                    builder.append("parentName：${result.parentFolderName}\n")
                    bing.tvCompressInfo.text = builder.toString()
                }
            }
        }
    }

    private val Long.M: String
        @SuppressLint("DefaultLocale")
        get() {
            val megabytes = this / (1024.0 * 1024.0)
            return String.format("%.2f MB", megabytes)
        }
}