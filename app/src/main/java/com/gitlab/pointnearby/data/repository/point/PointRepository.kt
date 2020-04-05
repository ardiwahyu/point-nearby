package com.gitlab.pointnearby.data.repository.point

import androidx.lifecycle.MutableLiveData
import com.gitlab.pointnearby.data.remote.RequestService
import com.gitlab.pointnearby.data.remote.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PointRepository @Inject constructor(private val requestService: RequestService) {
    private val error = MutableLiveData<String>()
    private val loading = MutableLiveData<Boolean>()
    private val pointResult = MutableLiveData<List<Location>>()

    suspend fun getPoints(latitude: Double, longitude: Double, count: Int): PointResponse{
        return withContext(Dispatchers.IO){
            loading.postValue(true)
            val pointResponse = requestService.getPoint(latitude, longitude, count)
            if (pointResponse.isSuccessful){
                loading.postValue(false)
                pointResult.postValue(pointResponse.body())
            }else{
                loading.postValue(false)
                error.postValue(pointResponse.message())
            }
            return@withContext PointResponse(error, loading, pointResult)
        }
    }
}