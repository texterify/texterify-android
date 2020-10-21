package com.texterify.android.ota

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class TexterifyRelease(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "data") val data: I18NData,
)
