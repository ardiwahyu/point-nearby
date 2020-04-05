package com.gitlab.pointnearby.data.repository.point

import androidx.lifecycle.LiveData
import com.gitlab.pointnearby.data.remote.model.Location

data class PointResponse (
    val error: LiveData<String>,
    val loading: LiveData<Boolean>,
    val pointResult: LiveData<List<Location>>
)