package com.iptv.nayantv.model

import com.google.gson.annotations.SerializedName

data class Channel(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("category") val category: String,
    @SerializedName("logo") val logo: String,
    @SerializedName("m3u8") val m3u8: String
)

data class ChannelResponse(
    @SerializedName("total") val total: Int,
    @SerializedName("categories") val categories: Map<String, List<Channel>>
)
