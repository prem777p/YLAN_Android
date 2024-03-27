package com.prem.ylan.api


import androidx.annotation.RawRes
import com.prem.ylan.model.DirectoryFileInfo
import com.prem.ylan.model.OsInfo
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RetrofitService {
        @POST("/receivepath")
        fun getDirectoryFile(@Body path : String) : Call<DirectoryFileInfo>?

        @GET("/osinfo")
        fun getOsInfo(): Call<OsInfo?>?
}

