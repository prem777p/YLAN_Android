package com.prem.ylan.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.prem.ylan.R
import com.prem.ylan.adapter.FileManagerAdapter
import com.prem.ylan.api.DirectoryFileFactory
import com.prem.ylan.api.RetrofitHelper
import com.prem.ylan.api.repository.DirectoryFileRepository
import com.prem.ylan.databinding.ActivityFileManagerBinding
import com.prem.ylan.model.PathManager
import com.prem.ylan.viewmodel.DirectoryFileViewModel


class FileManager : AppCompatActivity() {

    private lateinit var directoryFileViewModel: DirectoryFileViewModel
    private lateinit var binding: ActivityFileManagerBinding
    lateinit var path : String
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_file_manager)


        binding.uploadBtn.setOnClickListener{
            Toast.makeText(this,"Upload Comes Soon",Toast.LENGTH_SHORT).show()
        }


        val repository = DirectoryFileRepository(RetrofitHelper.getRetrofitInstance())
        directoryFileViewModel = ViewModelProvider(this,DirectoryFileFactory(repository,PathManager.getPathInstance().path))[DirectoryFileViewModel::class.java]

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

        directoryFileViewModel.directoryFile.observe(this) {
            val fileManagerAdapter = FileManagerAdapter(this, it.files, it.directories)
            binding.DirFileRv.adapter = fileManagerAdapter

        }
        binding.DirFileRv.layoutManager = layoutManager

        binding.fileViewBtnIv.setOnClickListener{
            startActivity(Intent(this, com.prem.ylan.activity.PathManager::class.java))
        }
    }


    override fun onBackPressed() {
       try {
        PathManager.removePath()
       }catch (_:Exception){}
        super.onBackPressed()
    }
}