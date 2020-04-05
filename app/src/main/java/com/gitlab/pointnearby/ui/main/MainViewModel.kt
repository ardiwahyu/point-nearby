package com.gitlab.pointnearby.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitlab.pointnearby.data.repository.point.PointRepository
import com.gitlab.pointnearby.data.repository.point.PointResponse
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainViewModel @Inject constructor(
    private val pointRepository: PointRepository
): ViewModel() {

    private val pointResponse = MutableLiveData<PointResponse>()

    val error = Transformations.switchMap(pointResponse){ it.error }
    val loading = Transformations.switchMap(pointResponse){ it.loading }
    val location = Transformations.switchMap(pointResponse){ it.pointResult }

    fun getPoints(latitude: Double, longitude: Double, count: Int){
        viewModelScope.launch {
            pointResponse.postValue(pointRepository.getPoints(latitude, longitude, count))
        }
    }
}