package com.abedelazizshe.lightcompressorlibrary.config

import androidx.annotation.WorkerThread
import kotlin.math.max
import kotlin.math.min

/**
 * 压缩转换策略
 * @author petterp
 */
interface ICompressEngine {
    @WorkerThread
    fun convert(model: CompressEngineModel): CompressEngineModel
}

/**
 * 自定义的Echo视频压缩策略
 * 根据width,height,curBit,duration 来计算出合适的压缩配置
 * */
object VideoDefaultCompressEngine : ICompressEngine {
    private const val MAX_HEIGHT = 1280.00
    private const val MAX_WIDTH = 720.00
    private const val MIN_BITRATE = 2000000
    private const val MAX_BITRATE = 3000000
    private const val ECHO_BITRATE_SALT = 200000       // 动态减BPS(减盐)
    override fun convert(model: CompressEngineModel): CompressEngineModel {
        // 计算新的比特率，根据分辨率缩减比例，进行减bps
        val (bit, width, height, duration) = model
        val (newW, newH) = resizeToFit(width, height)
        val specific = newW / width
        val newBps = newBps(specific)
        return model.copy(
            bitrate = Bitrate(min(newBps, bit.bps)),
            width = newW,
            height = newH
        )
    }

    private fun newBps(specific: Double): Int {
        // 计算新的比特率，考虑默认比特率和加盐值
        val newBps = (MAX_BITRATE + (specific - 1) * ECHO_BITRATE_SALT).toInt()
        return max(min(newBps, MAX_BITRATE), MIN_BITRATE)
    }

    private fun resizeToFit(
        width: Double,
        height: Double,
        maxWidth: Double = MAX_WIDTH,
        maxHeight: Double = MAX_HEIGHT
    ): Pair<Double, Double> {
        val aspectRatio = width / height
        var newWidth = width
        var newHeight = height

        if (newWidth > maxWidth) {
            newWidth = maxWidth
            newHeight = newWidth / aspectRatio
        }

        if (newHeight > maxHeight) {
            newHeight = maxHeight
            newWidth = newHeight * aspectRatio
        }
        return Pair(newWidth, newHeight)
    }
}


/**
 * 压缩策略模型
 * @param bitrate 当前比特率
 * @param width 当前宽度
 * @param height 当前高度
 * @param duration 时长
 */
data class CompressEngineModel(
    val bitrate: Bitrate,
    val width: Double,
    val height: Double,
    val duration: Duration
) {
    override fun toString(): String {
        return "CompressEngineModel(bitrate=${bitrate.mbps}, width=$width, height=$height, duration=${duration.seconds})"
    }
}

@JvmInline
value class Bitrate(val bps: Int) {
    val mbps: Float
        get() = bps / 1000000f
    val kbps: Float
        get() = bps / 1000f
}

@JvmInline
value class Duration(val milliseconds: Long) {
    val seconds: Long
        get() = milliseconds / 1000 / 1000
    val minutes: Long
        get() = seconds / 60
    val hours: Long
        get() = minutes / 60
}