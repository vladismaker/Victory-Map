package com.application.victorymap.domain.usecase

import androidx.lifecycle.MutableLiveData
import com.application.victorymap.domain.repository.LocationRepository
import com.application.victorymap.domain.repository.SealedLocationListPointsResult
import com.application.victorymap.domain.repository.SealedLocationUserPointResult
import com.yandex.mapkit.geometry.Point

class GetUserLocationPointUseCase(private val locationRepository: LocationRepository) {
    val sealedLocationListPointsResultLiveData = MutableLiveData<SealedLocationListPointsResult>()
    private val officePoint = Point(56.833742, 60.635716)

    fun execute() {
        locationRepository.fetchUserLocation {
            when (it) {
                is SealedLocationUserPointResult.SuccessPoints -> {
                    val userLocationPoint = it.points
                    val points = listOf(userLocationPoint, officePoint)
                    //Вернуть points
                    sealedLocationListPointsResultLiveData.postValue(SealedLocationListPointsResult.SuccessPointsSealed(points))
                }
                is SealedLocationUserPointResult.ErrorMessage -> {
                    //Вернуть String message
                    sealedLocationListPointsResultLiveData.postValue(SealedLocationListPointsResult.ErrorMessage(it.message))
                }
            }
        }
    }
}