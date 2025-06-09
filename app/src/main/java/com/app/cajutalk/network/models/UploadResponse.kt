package com.app.cajutalk.network.models

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("url")
    val url: String
)