package com.abedelazizshe.lightcompressorlibrary.video

import com.abedelazizshe.lightcompressorlibrary.config.CompressId

data class CompressResult(
    val id: CompressId,
    val success: Boolean,
    val failureMessage: String?,
    val size: Long = 0,
    val path: String? = null,
    val time: Long = 0,
)
