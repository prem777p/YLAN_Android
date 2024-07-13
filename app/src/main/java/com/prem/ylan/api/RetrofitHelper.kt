package com.prem.ylan.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.prem.ylan.model.PathManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitHelper {
    private var BASE_URL = "http://${PathManager.getPathInstance().ipAddress}:8080"
    private var retrofitService : RetrofitService? = null
    private var instanceCheck : Boolean = false

    fun setUrl(url: String){
        BASE_URL = "http://${url}:8080"
    }

    fun setHelperInstance(){
        instanceCheck = true
    }

    private var gson : Gson= GsonBuilder()
        .setLenient()
        .create()

    private fun getInstance(URl: String): Retrofit{
        return Retrofit.Builder()
            .baseUrl(URl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getRetrofitInstance(): RetrofitService {
        if (retrofitService != null && instanceCheck) {
            return retrofitService as RetrofitService
        }else{
            retrofitService = getInstance(BASE_URL).create(RetrofitService::class.java)
            return retrofitService as RetrofitService
        }
    }
}