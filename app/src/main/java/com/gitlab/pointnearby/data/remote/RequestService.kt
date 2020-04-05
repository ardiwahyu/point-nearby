package com.gitlab.pointnearby.data.remote

import com.gitlab.pointnearby.data.remote.model.Location
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RequestService {
    @GET("point.php")
    suspend fun getPoint(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("point") point: Int
    ): Response<List<Location>>
}