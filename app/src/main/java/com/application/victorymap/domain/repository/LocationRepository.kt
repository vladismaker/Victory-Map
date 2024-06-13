package com.application.victorymap.domain.repository

interface LocationRepository {
    fun fetchUserLocation(callback: (SealedLocationUserPointResult)-> Unit)
}