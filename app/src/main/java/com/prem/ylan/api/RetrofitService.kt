package com.prem.ylan.api


import com.prem.ylan.model.DirectoryFileInfo
import com.prem.ylan.model.OsInfo
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitService {
        @POST("/receivepath")
        fun getDirectoryFile(@Body path : String) : Call<DirectoryFileInfo>?

        @GET("/osinfo")
        fun getOsInfo(): Call<OsInfo?>?

        @Multipart
        @POST("/upload")
        suspend fun uploadFile(@Part file: MultipartBody.Part, @Part("path") path: RequestBody): Response<String>

}

