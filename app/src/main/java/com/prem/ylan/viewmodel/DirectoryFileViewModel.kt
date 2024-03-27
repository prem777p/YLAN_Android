package com.prem.ylan.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prem.ylan.model.DirectoryFileInfo
import com.prem.ylan.api.repository.DirectoryFileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.file.Path

class DirectoryFileViewModel(private val repository: DirectoryFileRepository, path: String): ViewModel() {
    init {
        viewModelScope.launch(Dispatchers.IO){
            repository.getDirectoryFile(path)
        }
    }

    val directoryFile : LiveData<DirectoryFileInfo>
    get() = repository.directoryFile

}