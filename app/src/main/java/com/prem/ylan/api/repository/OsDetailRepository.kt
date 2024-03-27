package com.prem.ylan.api.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.prem.ylan.api.RetrofitService
import com.prem.ylan.model.OsInfo
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OsDetailRepository(private val retrofitService: RetrofitService) {
    private val osLiveData = MutableLiveData<OsInfo>()
    val os : LiveData<OsInfo>
        get() = osLiveData

    suspend fun getOs() {
        retrofitService.getOsInfo()?.enqueue(object : Callback<OsInfo?> {
            override fun onResponse(call: Call<OsInfo?>, response: Response<OsInfo?>) {
                osLiveData.postValue(response.body())
            }

            override fun onFailure(call: Call<OsInfo?>, t: Throwable) {
            }
        })
    }
}