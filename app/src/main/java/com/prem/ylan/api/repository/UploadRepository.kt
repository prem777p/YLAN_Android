package com.prem.ylan.api.repository

import com.prem.ylan.api.RetrofitService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UploadRepository(private val retrofitService: RetrofitService) {

    suspend fun uploadFile(file: File,path: String): String? {
        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val pathBody = RequestBody.create(MediaType.parse("text/plain"), path)

        return retrofitService.uploadFile(body,pathBody).body()
    }
}