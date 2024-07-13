package com.prem.ylan.activity

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.prem.ylan.R
import com.prem.ylan.adapter.FileManagerAdapter
import com.prem.ylan.api.DirectoryFileFactory
import com.prem.ylan.api.RetrofitHelper
import com.prem.ylan.api.repository.DirectoryFileRepository
import com.prem.ylan.api.repository.UploadRepository
import com.prem.ylan.databinding.ActivityFileManagerBinding
import com.prem.ylan.model.PathManager
import com.prem.ylan.viewmodel.FileManagerViewModel
import java.io.File


class FileManager : AppCompatActivity() {

    private lateinit var fileManagerViewModel: FileManagerViewModel
    private lateinit var binding: ActivityFileManagerBinding


    private val readFile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ it ->
        it.data?.data?.let {
            val filename = it.path?.let { it1 -> File(it1).name }
            if (filename != null) {
//               getFileByURI(it, filename)?.let { file -> fileManagerViewModel.uploadFile(file) }
               val file =  getFileByURI(it, filename)
                if (file != null) {
                Log.d("file size", file.length().toString())
                    fileManagerViewModel.uploadFile(file)
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_manager)


        binding.uploadBtn.setOnClickListener{
                checkPermission()
        }


        val directoryFileRepository = DirectoryFileRepository(RetrofitHelper.getRetrofitInstance())
        val uploadRepository = UploadRepository(RetrofitHelper.getRetrofitInstance())
        fileManagerViewModel = ViewModelProvider(this,DirectoryFileFactory(directoryFileRepository,PathManager.getPathInstance().path, uploadRepository))[FileManagerViewModel::class.java]

        if (PathManager.getPathInstance().os.contains("linux")){
            binding.pathTv.text = PathManager.getPathInstance().path.replace("/","  >  ")
        }
        else if (PathManager.getPathInstance().os.contains("window")){
            val path =  PathManager.getPathInstance().path.replace("/","  >  ")
            binding.pathTv.text = path.replace("\\\\","  >  ")
        }
        else if (PathManager.getPathInstance().os.contains("mac")){
            binding.pathTv.text = PathManager.getPathInstance().path.replace("/","  >  ")
        }



        val layoutManager: LayoutManager = LinearLayoutManager(this)

        fileManagerViewModel.directoryFile.observe(this) {
            val fileManagerAdapter = FileManagerAdapter(this, it.files, it.directories)
            binding.DirFileRv.adapter = fileManagerAdapter

        }
        binding.DirFileRv.layoutManager = layoutManager

        binding.fileViewBtnIv.setOnClickListener{
            startActivity(Intent(this, com.prem.ylan.activity.PathManager::class.java))
        }
    }

    fun readFile(){
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type= "*/*"
        readFile.launch(intent)
    }
    private fun getFileByURI(uri: Uri, fileName: String): File? {
        return  try {
            val inputStream = this.contentResolver.openInputStream(uri)
            val outputFile = File(
                filesDir,
                fileName
            )
            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            return outputFile

        }catch(e : Exception){
            null
        }
    }

    private fun checkPermission(){
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted() || android.os.Build.VERSION.SDK_INT >= 33) {
                        readFile()
                    } else {
                        Toast.makeText(this@FileManager,"Please Allow Permission", Toast.LENGTH_SHORT ).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) { /* ... */
                }
            }).check()
    }

    override fun onBackPressed() {
       try {
        PathManager.removePath()
       }catch (_:Exception){}
        super.onBackPressed()
    }
}