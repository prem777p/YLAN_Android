package com.prem.ylan.model


data class Path(var path: String, var os : String, var ipAddress : String)

object PathManager {
    // Singleton instance
    private val instance: Path by lazy { Path("", os = "", ipAddress = "") }

    // Accessor method to get the singleton instance
    fun getPathInstance(): Path {
        return instance
    }

    fun addPath(newPath : String) {
        instance.path += newPath
    }

    fun addOs(os : String) {
        instance.os = os
    }

    fun addIpAddress(ip : String) {
        instance.ipAddress = ip

    }

    fun removePath() {
        instance.path = instance.path.substring(0,instance.path.lastIndexOf("/"))
    }
}