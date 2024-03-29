package com.prem.ylan.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import com.prem.ylan.R
import com.prem.ylan.databinding.ActivityPathManagerBinding
import com.prem.ylan.model.PathManager


class PathManager : AppCompatActivity() {
    private lateinit var binding : ActivityPathManagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_path_manager)

        binding.changepathBtn.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.changepathBtn, InputMethodManager.SHOW_IMPLICIT)
            } else {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.changepathBtn.windowToken, 0)
            }
        }

    binding.changepathBtn.setOnClickListener{
        val newPath = binding.newPathEdt.text.toString()
        if (newPath.isNotEmpty()) {
            PathManager.getPathInstance().path = newPath

            val intent = Intent(this, FileManager::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            Toast.makeText(this,"Previous all Activity Closed",Toast.LENGTH_SHORT).show()

        }else{
            Toast.makeText(this,"Enter Correct Path",Toast.LENGTH_SHORT).show()
        }
    }
    }
}