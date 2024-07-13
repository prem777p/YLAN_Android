package com.prem.ylan.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prem.ylan.api.repository.DirectoryFileRepository
import com.prem.ylan.api.repository.UploadRepository
import com.prem.ylan.viewmodel.FileManagerViewModel

class DirectoryFileFactory(private val repository: DirectoryFileRepository, val path : String, private val uploadRepository: UploadRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return FileManagerViewModel(repository, path, uploadRepository) as T
    }
}