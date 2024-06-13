package com.application.victorymap.domain.repository

import com.yandex.mapkit.geometry.Point

sealed class SealedLocationListPointsResult {
    data class SuccessPointsSealed(val points: List<Point>) : SealedLocationListPointsResult()
    data class ErrorMessage(val message: String) : SealedLocationListPointsResult()
}