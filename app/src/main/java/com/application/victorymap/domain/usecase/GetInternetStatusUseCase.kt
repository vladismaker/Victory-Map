package com.application.victorymap.domain.usecase

import com.application.victorymap.domain.repository.MapScreenRepository

class GetInternetStatusUseCase(private val mapScreenRepository: MapScreenRepository) {
    fun execute():Boolean{
        return mapScreenRepository.getInternetStatus()
    }
}