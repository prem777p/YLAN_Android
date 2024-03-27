package com.prem.ylan.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.prem.ylan.api.repository.DirectoryFileRepository
import com.prem.ylan.viewmodel.DirectoryFileViewModel

class DirectoryFileFactory(private val repository: DirectoryFileRepository,val path : String) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DirectoryFileViewModel(repository, path) as T
    }
}