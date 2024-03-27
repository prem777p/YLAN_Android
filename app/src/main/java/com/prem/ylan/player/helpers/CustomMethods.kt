package com.prem.ylan.player.helpers

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Handler
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.prem.ylan.R
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.min
import kotlin.math.pow

object CustomMethods {
    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) {
            return "0 B"
        }
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
        return String.format(
            Locale.getDefault(), "%.2f %s", sizeInBytes / 1024.00.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }

    fun formatModifiedDate(seconds: Long): String {
        val date = Date(seconds * 1000)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return sdf.format(date)
    }

    fun formatDuration(duration: Long): String {
        // Convert milliseconds to seconds
        val seconds = duration / 1000

        // Calculate hours, minutes, and remaining seconds
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val remainingSeconds = seconds % 60

        // Format the duration as "hh:mm:ss" with Locale.US
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, remainingSeconds)
    }

    fun getVersionName(context: Context): String {
        val packageInfo: PackageInfo
        packageInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            throw RuntimeException(e)
        }
        return packageInfo.versionName
    }

    fun hideSoftKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun isValidURL(url: String): Boolean {
        val firstEightCharacters = url.substring(
            0,
            min(url.length.toDouble(), 8.0).toInt()
        )
        return if (firstEightCharacters.lowercase(Locale.getDefault()).startsWith("ftp://")) {
            true
        } else if (firstEightCharacters.lowercase(Locale.getDefault()).startsWith("http://")) {
            true
        } else if (firstEightCharacters.lowercase(Locale.getDefault()).startsWith("rtmp://")) {
            true
        } else firstEightCharacters.lowercase(Locale.getDefault()).startsWith("https://")
    }

    fun getFileName(url: String?): String? {
        return try {
            val urlObj = URL(url)
            val path = urlObj.path
            val index = path.lastIndexOf("/")
            if (index != -1) {
                path.substring(index + 1)
            } else {
                null
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun errorAlert(
        activity: Activity,
        errorTitle: String?,
        errorBody: String?,
        actionButton: String?,
        shouldGoBack: Boolean
    ) {
        if (!activity.isFinishing) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(errorTitle)
            builder.setMessage(errorBody)
            builder.setIcon(R.drawable.icon_warning)
            builder.setPositiveButton(
                actionButton
            ) { dialogInterface: DialogInterface, i: Int ->
                if (shouldGoBack) {
                    activity.finish()
                } else {
                    dialogInterface.dismiss()
                }
            }
            builder.setNegativeButton("Copy") { dialog: DialogInterface?, which: Int ->
                val clipboardManager =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = ClipData.newPlainText("text", errorBody)
                clipboardManager.setPrimaryClip(clipData)
                Handler().postDelayed({ activity.finish() }, 1000)
            }
            val dialog = builder.create()
            dialog.show()
        }
    }
}
