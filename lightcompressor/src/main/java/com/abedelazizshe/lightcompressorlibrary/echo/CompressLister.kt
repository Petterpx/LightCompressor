package com.abedelazizshe.lightcompressorlibrary.echo

import androidx.annotation.WorkerThread


typealias CompressStartListener = suspend (CompressId) -> Unit
typealias CompressSuccessListener = suspend (CompressId, Long, String?, Long) -> Unit
typealias CompressFailureListener = suspend (CompressId, String) -> Unit
typealias CompressProgressListener = suspend (CompressId, Float) -> Unit
typealias CompressCancelledListener = suspend (CompressId) -> Unit

interface CompressLister {
    fun onStart(listener: CompressStartListener)
    fun onSuccess(listener: CompressSuccessListener)
    fun onFailure(listener: CompressFailureListener)
    fun onProgress(listener: CompressProgressListener)
    fun onCancelled(listener: CompressCancelledListener)
}


class CompressListerImpl : CompressLister {
    internal var startListener: CompressStartListener? = null
    internal var successListener: CompressSuccessListener? = null
    internal var failureListener: CompressFailureListener? = null
    internal var progressListener: CompressProgressListener? = null
    internal var cancelledListener: CompressCancelledListener? = null

    override fun onStart(listener: CompressStartListener) {
        startListener = listener
    }

    override fun onSuccess(listener: CompressSuccessListener) {
        successListener = listener
    }

    override fun onFailure(listener: CompressFailureListener) {
        failureListener = listener
    }

    override fun onProgress(listener: CompressProgressListener) {
        progressListener = listener
    }

    override fun onCancelled(listener: CompressCancelledListener) {
        cancelledListener = listener
    }
}
