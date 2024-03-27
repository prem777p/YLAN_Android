package com.prem.ylan.model


data class DownloadStatus(var totalDownloadSize: Int, var downloadSize : Int)

object DownloadStatusManager {
    // Singleton instance
    private val instance: DownloadStatus by lazy { DownloadStatus(1, 1) }

    // Accessor method to get the singleton instance
    fun getPathInstance(): DownloadStatus {
        return instance
    }

    fun addTotalDownladSize(totalDownloadSize: Int) {
        instance.totalDownloadSize = totalDownloadSize
    }

    fun addDownladSize(downloadSize: Int) {
        instance.downloadSize = downloadSize
    }
}