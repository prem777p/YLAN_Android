package com.prem.ylan.api.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prem.ylan.api.RetrofitService
import com.prem.ylan.model.DirectoryFileInfo
import retrofit2.Call
import retrofit2.Callback

class DirectoryFileRepository(private val retrofitService: RetrofitService) {
    private val directoryFileLiveData = MutableLiveData<DirectoryFileInfo>()
    val directoryFile : LiveData<DirectoryFileInfo>
        get() = directoryFileLiveData

    suspend fun getDirectoryFile(path : String) {
        retrofitService.getDirectoryFile(path)?.enqueue(object : Callback<DirectoryFileInfo?> {
            override fun onResponse(
                call: Call<DirectoryFileInfo?>,
                response: retrofit2.Response<DirectoryFileInfo?>
            ) {
                directoryFileLiveData.postValue(response.body())
            }
            override fun onFailure(call: Call<DirectoryFileInfo?>, t: Throwable) {
            }
        })
    }
}