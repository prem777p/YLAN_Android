 package com.prem.ylan.adapter

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.prem.ylan.R
import com.prem.ylan.activity.FileManager
import com.prem.ylan.activity.Player
import com.prem.ylan.api.DownloadManager
import com.prem.ylan.model.PathManager
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


 @OptIn(UnstableApi::class)
 class FileManagerAdapter(var context : Context, file: List<String>?, directories : List<String>?) : RecyclerView.Adapter<FileManagerAdapter.ViewHolder>() {

    private var directoryFileList = ArrayList<String>()
    private var size :Int = 0

    init {
        if (directories != null) {
            directoryFileList.addAll(directories)
        }
        if (file != null) {
            directoryFileList.addAll(file)
        }
        if (directories != null) {
            size = directories.size
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.file_structure_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(directoryFileList[position],size,position)
    }

    override fun getItemCount(): Int {
        return directoryFileList.size
    }
     inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
         fun bind(data: String, size : Int, position: Int) {
             // Bind data to UI elements

             if (size <= position && data.contains(".mp4") || data.contains(".mkv") || data.contains(".MOV")) {
                 itemView.findViewById<ImageView>(R.id.icon_iv).setImageResource(R.drawable.icon_video_library_mp4)
                 itemView.findViewById<ImageView>(R.id.download_btn).setImageResource(R.drawable.icon_downloading_fill)
                 itemView.findViewById<ImageView>(R.id.download_btn).visibility = View.VISIBLE

             }
             else if (size <= position){
                 itemView.findViewById<ImageView>(R.id.icon_iv).setImageResource(R.drawable.icon_file_text)
                 itemView.findViewById<ImageView>(R.id.download_btn).setImageResource(R.drawable.icon_downloading_fill)
                 itemView.findViewById<ImageView>(R.id.download_btn).visibility = View.VISIBLE
             }
             if (size > position){
                 itemView.findViewById<ImageView>(R.id.icon_iv).setImageResource(R.drawable.icon_folders)
                 itemView.findViewById<ImageView>(R.id.download_btn).visibility = View.INVISIBLE
             }

             val fileNameTv = itemView.findViewById<TextView>(R.id.file_name_tv)
             fileNameTv.text = data
             itemView.findViewById<ImageView>(R.id.download_btn).setOnClickListener {

                 val url = "http://${PathManager.getPathInstance().ipAddress}:8080/download/${fileNameTv.text}?folderPath=${PathManager.getPathInstance().path}"

                 val downloadProgressBar = itemView.findViewById<ProgressBar>(R.id.download_progressbar)
                 downloadProgressBar.visibility = View.VISIBLE
                 DownloadManager.checkPermission(context, url,downloadProgressBar)
             }
             itemView.findViewById<LinearLayout>(R.id.file_open_ll).setOnClickListener{
                // if Position is directory open and add path in PathManager class
                 if (size > position){
                     val intent = Intent(context, FileManager::class.java)
                     if (PathManager.getPathInstance().os.contains("linux")){
                         PathManager.addPath("/${itemView.findViewById<TextView>(R.id.file_name_tv).text}")
                     }
                     else if (PathManager.getPathInstance().os.contains("window")){
                         PathManager.addPath("/${itemView.findViewById<TextView>(R.id.file_name_tv).text}")
                     }
                     else if (PathManager.getPathInstance().os.contains("mac")){
//                        intent.putExtra("path","/${itemView.findViewById<TextView>(R.id.file_name_tv).text}")
                     }

                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                     context.startActivity(intent)
                 }

                 // if it is video file then open it  in player activity
                 if (size <= position && data.contains(".mp4") || data.contains(".mkv") || data.contains(".MOV")){

                    val intent = Intent(context, Player::class.java)
                    if (PathManager.getPathInstance().os.contains("mac")){
//                        intent.putExtra("path","/${itemView.findViewById<TextView>(R.id.file_name_tv).text}")
                     }
                     else{
                         intent.putExtra("path","${PathManager.getPathInstance().path}/${itemView.findViewById<TextView>(R.id.file_name_tv).text}")
                         intent.putExtra("mediaFileName",itemView.findViewById<TextView>(R.id.file_name_tv).text)
                     }
                     intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                     context.startActivity(intent)
                 }
             }

            itemView.findViewById<LinearLayout>(R.id.file_open_ll).setOnLongClickListener {

                if (size <= position && data.contains(".mp4") || data.contains(".mkv") || data.contains(".MOV")) {
                    val url = "http://" + PathManager.getPathInstance().ipAddress + ":8080/stream/url?path=/${PathManager.getPathInstance().path.replace(" ", "%20")}/${(fileNameTv.text as String).replace(" ", "%20")}"
                    val share = Intent(Intent.ACTION_SEND)
                    share.putExtra(Intent.EXTRA_TEXT, url)
                    share.type = "text/plain"
                    
                    context.startActivity(Intent.createChooser(share,"Share"))
                }
                return@setOnLongClickListener true
            }

            // Bind other data...
        }

    }
}