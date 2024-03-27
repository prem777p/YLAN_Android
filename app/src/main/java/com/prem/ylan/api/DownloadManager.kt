package com.prem.ylan.api

import android.Manifest
import android.content.Context
import android.os.Environment
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.ProgressBar
import android.widget.Toast
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.prem.ylan.model.DownloadStatusManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

object DownloadManager {
    private fun downloadFile(context: Context, url: String, downloadProgressBar: ProgressBar) {

        val title = URLUtil.guessFileName(url, null, null)
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), title)

        Thread {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                val cookie = CookieManager.getInstance().getCookie(url)
                connection.setRequestProperty("cookie", cookie)
                connection.connect()

                val inputStream = BufferedInputStream(connection.inputStream)
                val outputStream = FileOutputStream(file)

                val totalSize = connection.contentLength.toLong()
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var downloadedSize : Long = 0

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)

                    downloadedSize += bytesRead
                    val percentage : Int = ((downloadedSize * 100) / totalSize).toInt()
                    downloadProgressBar.setProgress(percentage,true)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()


                // Notify completion
//                        if (downloadProgressBar.progress == 100) {
//                            Toast.makeText(context, "Download completed", Toast.LENGTH_SHORT).show()
//                        }

            } catch (e: IOException) {
                e.printStackTrace()
                // Handle error
                Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }


    fun checkPermission(context: Context, url: String, downloadProgressBar: ProgressBar) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted() || android.os.Build.VERSION.SDK_INT >= 33) {
                        downloadFile(context, url, downloadProgressBar)
                    } else {
                        Toast.makeText(
                            context,
                            "Please Allow Permission",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                }
            }).check()
    }
}