package com.prem.ylan.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prem.ylan.model.DirectoryFileInfo
import com.prem.ylan.api.repository.DirectoryFileRepository
import com.prem.ylan.api.repository.UploadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class FileManagerViewModel(private val repository: DirectoryFileRepository, var path: String, private val uploadRepository: UploadRepository): ViewModel() {

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean> = _uploadStatus

    init {
        viewModelScope.launch(Dispatchers.IO){
            repository.getDirectoryFile(path)
        }
    }

    val directoryFile : LiveData<DirectoryFileInfo>
    get() = repository.directoryFile


    fun uploadFile(file: File) {
        viewModelScope.launch {
            try {
                val response = uploadRepository.uploadFile(file, path)
                Log.d("filemanager","run "+response.toString())
                _uploadStatus.postValue(response != null)
            } catch (e: Exception) {
                Log.d("filemanager","run "+e)
                _uploadStatus.postValue(false)
            }
        }
    }

}