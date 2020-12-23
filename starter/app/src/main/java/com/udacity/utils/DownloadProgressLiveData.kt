package com.udacity.utils

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

data class DownloadItem(val bytesDownloadedSoFar: Long = -1, val totalSizeBytes: Long = -1, val status: Int)

class DownloadProgressLiveData(private val application: Application, private val requestId: Long) : LiveData<DownloadItem>(),
    CoroutineScope {

    private val downloadManager by lazy {
        application.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    }

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    private val query = DownloadManager.Query()

    init {
        query.setFilterById(this.requestId)
    }

    @InternalCoroutinesApi
    override fun onActive() {
        super.onActive()
        launch {
            while (isActive) {
                val cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL,
                        DownloadManager.STATUS_PENDING,
                        DownloadManager.STATUS_FAILED,
                        DownloadManager.STATUS_PAUSED -> postValue(DownloadItem(status = status))
                        else -> {
                            val bytesDownloadedSoFar = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)).toLong()
                            val totalSizeBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).toLong()
                            postValue(
                                DownloadItem(
                                    bytesDownloadedSoFar,
                                    totalSizeBytes,
                                    status
                                )
                            )
                        }
                    }
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED)
                        cancel()
                } else {
                    postValue(DownloadItem(status = DownloadManager.STATUS_FAILED))
                    cancel()
                }
                cursor.close()
                delay(500)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
    }

}