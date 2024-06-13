package com.application.victorymap.domain.repository

import com.yandex.mapkit.geometry.Point

sealed class SealedLocationUserPointResult {
    data class SuccessPoints(val points: Point) : SealedLocationUserPointResult()
    data class ErrorMessage(val message: String) : SealedLocationUserPointResult()
}