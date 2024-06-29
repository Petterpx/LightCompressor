package com.abedelazizshe.lightcompressor.demo

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.CamcorderProfile
import android.net.Uri
import android.os.Build
import com.luck.picture.lib.basic.PictureSelectionModel
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.interfaces.OnExternalPreviewEventListener
import com.luck.picture.lib.interfaces.OnResultCallbackListener
import com.luck.picture.lib.utils.ActivityCompatHelper
import com.luck.picture.lib.utils.DateUtils
import java.io.File
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Create by Crazy on 2021/1/21
 * PictureSelectorHelper : Echo设置图片选择器的基础操作
 */
object PictureSelectorHelper {

    internal var externalPreViewListener: OnExternalPreviewEventListener? = null

    fun initPreviewListener(listener: OnExternalPreviewEventListener) {
        this.externalPreViewListener = listener
    }

    fun create(activity: Activity, imageSize: Int): PictureSelectionModel = createByMimeType(
        activity,
        if (imageSize == 0) SelectMimeType.ofAll() else SelectMimeType.ofImage()
    ).setMaxSelectNum(9 - imageSize)

    fun create(activity: Activity, images: List<LocalMedia>): PictureSelectionModel =
        createByMimeType(activity, SelectMimeType.ofAll())
            .setMaxSelectNum(9)
            .setSelectedData(images)

    fun createMaxSize(activity: Activity, maxSelectSize: Int) = createByMimeType(
        activity,
        SelectMimeType.ofImage()
    ).setMaxSelectNum(maxSelectSize)

    fun createByMimeType(activity: Activity?, mineType: Int) = PictureSelector.create(activity)
        .openGallery(mineType)
        .setImageEngine(GlideEngine.createGlideEngine())
        .setMaxVideoSelectNum(1)
        .setImageSpanCount(4)
        .isPreviewImage(true)
        .isPreviewVideo(true)
        .isEmptyResultReturn(false)


    // 以下为协程封装,推荐使用
    suspend fun launchSingleImage(context: Context): LocalMedia? {
        return launchMultiImage(context, 1).firstOrNull()
    }

    suspend fun launchSingleVideo(context: Context): LocalMedia? {
        val result = launchMultiVideo(context, 1)
        return result.firstOrNull()
    }

    suspend fun launchMultiImage(context: Context, maxSum: Int = 1): List<LocalMedia> {
        val result = suspendCancellableCoroutine<List<LocalMedia>> { resume ->
            PictureSelector.create(context).openGallery(SelectMimeType.ofImage())
                .setImageEngine(GlideEngine.createGlideEngine())
                .isMaxSelectEnabledMask(true)
                .isFilterSizeDuration(true)
                .setMaxSelectNum(maxSum)
                .setOutputCameraVideoFileName("qd")
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: ArrayList<LocalMedia>?) {
                        if (result != null && result.size > 0) {
                            resume.resume(result)
                        } else {
                            resume.cancel()
                        }
                    }

                    override fun onCancel() {
                        resume.cancel()
                    }
                })
        }
        return result
    }

    suspend fun launchMultiVideo(
        context: Context,
        maxSum: Int = 1,
    ): List<LocalMedia> {
        val result = suspendCancellableCoroutine<List<LocalMedia>> { resume ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PictureSelector.create(context).openGallery(SelectMimeType.ofVideo())
                    .setImageEngine(GlideEngine.createGlideEngine())
                    .isMaxSelectEnabledMask(true)
                    .isFilterSizeDuration(true)
                    .setMaxSelectNum(maxSum)
                    .forResult(object : OnResultCallbackListener<LocalMedia> {
                        override fun onResult(result: ArrayList<LocalMedia>?) {
                            if (result != null && result.size > 0) {
                                resume.resume(result)
                            } else {
                                resume.cancel()
                            }
                        }

                        override fun onCancel() {
                            resume.cancel()
                        }
                    })
            }
        }
        return result
    }
}
