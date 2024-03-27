package com.prem.ylan.activity

import android.content.Intent
import android.os.Bundle
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


    binding.changepathBtn.setOnClickListener{
        var newPath = binding.newPathEdt.text.toString()
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