package com.prem.ylan.model

import com.google.gson.annotations.SerializedName

data class DirectoryFileInfo(

    @SerializedName("root_dir_name")
    val rootDirName: String,

    @SerializedName("directory")
    val directories: List<String>,

    @SerializedName("file")
    val files: List<String>

)
