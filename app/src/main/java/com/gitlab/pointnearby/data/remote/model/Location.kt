package com.gitlab.pointnearby.data.remote.model

import com.google.android.gms.maps.model.Marker
import com.google.gson.annotations.SerializedName

data class Location (
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    var distance: Int = 0,
    var name: String = "",
    var marker: Marker? = null
)