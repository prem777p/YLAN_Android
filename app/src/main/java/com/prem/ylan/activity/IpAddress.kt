package com.prem.ylan.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.prem.ylan.R
import com.prem.ylan.databinding.ActivityIpAddressBinding
import com.prem.ylan.model.PathManager
import com.prem.ylan.model.Urls
import com.prem.ylan.viewmodel.IpAddressViewModel


class IpAddress() : AppCompatActivity() {
    private lateinit var binding: ActivityIpAddressBinding
    private var ipAddress: String = ""
    private var ipAddressViewModel : IpAddressViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ip_address)

        ipAddress = getWifiIPv4Address(applicationContext)
        binding.ipEdtv.setText(ipAddress)

        binding.ipEdtv.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.ipEdtv, InputMethodManager.SHOW_IMPLICIT)
            } else {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.ipEdtv.windowToken, 0)
            }
        }

        binding.saveIpBtn.setOnClickListener{
            val ipRegex = """^(\d{1,3}\.){3}\d{1,3}$""".toRegex()
            if (ipRegex.matches(binding.ipEdtv.text.toString())) {
                if (binding.osTv.text.toString().isEmpty()) {
                    ipAddress = binding.ipEdtv.text.toString()

                    PathManager.addIpAddress(ipAddress)
                    ipAddressViewModel = IpAddressViewModel(this.application)

                    binding.osDetail = ipAddressViewModel


                    ipAddressViewModel!!.osName.observe(this) {
                        binding.osTv.text = it.os
                        binding.saveIpBtn.text = getString(R.string.next)
                        ipAddressViewModel!!.setOs(it.os)
                    }

                    ipAddressViewModel!!.getDestroyActivity().observe(this) { destroy ->
                        if (destroy) {
                            // Finish the activity
                            finish()
                        }
                    }
                }else{
                    ipAddressViewModel!!.saveBtn()
                }
            }else{
                Toast.makeText(this,"Enter Correct IP Address",Toast.LENGTH_SHORT).show()
            }
        }
        binding.getIp.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(Urls.GITHUB_IP_ADD)
                ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }


    private fun getWifiIPv4Address(context: Context): String {
        val wifiManager =
            context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wifiInfo = wifiManager.connectionInfo
        val ipAddress = wifiInfo.ipAddress

        // Convert IP address from integer to String
        return (ipAddress and 0xFF).toString() + "." +
                (ipAddress shr 8 and 0xFF) + "." +
                (ipAddress shr 16 and 0xFF) + "." +
                (ipAddress shr 24 and 0xFF)
    }



}