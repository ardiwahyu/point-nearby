package com.gitlab.pointnearby.data.local.sp

import android.content.Context
import android.content.SharedPreferences
import com.gitlab.pointnearby.data.remote.model.Location
import com.google.gson.Gson
import javax.inject.Inject

class StatusManager @Inject constructor(context: Context){
    companion object{
        internal const val SP_NAME = "point_nearby"
        const val LOCATION_KEY = "location"
    }

    private var sp: SharedPreferences = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)

    fun storeLocation(location: Location){
        val loc = Gson().toJson(location)
        sp.edit().putString(LOCATION_KEY, loc).apply()
    }

    fun getLocation(): Location{
        val defaultLocation = Gson().toJson(Location(-6.121435, 106.774124))
        return Gson().fromJson(sp.getString(LOCATION_KEY, defaultLocation), Location::class.java)
    }
}