package com.prem.ylan.viewmodel

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prem.ylan.R
import com.prem.ylan.activity.FileManager
import com.prem.ylan.api.RetrofitHelper
import com.prem.ylan.api.repository.OsDetailRepository
import com.prem.ylan.model.OsInfo
import com.prem.ylan.model.PathManager
import com.prem.ylan.model.Urls
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IpAddressViewModel(private var application: Application) : ViewModel() {
    private var osCheck: String = ""
    var retrofitHelper = RetrofitHelper.setUrl(PathManager.getPathInstance().ipAddress)
    private var osRepository: OsDetailRepository = OsDetailRepository(RetrofitHelper.getRetrofitInstance())
    private var destroyActivity: MutableLiveData<Boolean> = MutableLiveData<Boolean>();


    //
    init {

        viewModelScope.launch(Dispatchers.IO) {
            osRepository.getOs()
        }
    }

    val osName: LiveData<OsInfo>
        get() = osRepository.os


    fun setOs(text: String) {
        osCheck = text
    }

    fun saveBtn() {

        if (osCheck.isNotEmpty()) {
            RetrofitHelper.setHelperInstance()
            //get os function
            val intent = Intent(application, FileManager::class.java).setFlags(FLAG_ACTIVITY_NEW_TASK)

            if (osCheck.lowercase().contains("Linux".lowercase())){
                PathManager.addOs("linux")
                PathManager.addPath("/home")
            }
            else if (osCheck.lowercase().contains("Window".lowercase())){
                PathManager.addOs("window")
                PathManager.addPath("D:")
            }
            else if (osCheck.lowercase().contains("Mac".lowercase())){
                PathManager.addOs("mac")
                PathManager.addPath("/home")
            }

            application.startActivity(intent)
            destroyActivity()

        } else {
            Toast.makeText(application, "Wrong IP Address", Toast.LENGTH_SHORT).show()
        }
    }


    // Method to trigger destruction of the activity
    private fun destroyActivity() {
        destroyActivity.value = true;
    }

    // Getter for the LiveData object
    fun getDestroyActivity(): LiveData<Boolean> {
        return destroyActivity;
    }
}