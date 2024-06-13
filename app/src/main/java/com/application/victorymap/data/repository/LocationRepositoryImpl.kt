package com.application.victorymap.data.repository

import com.application.victorymap.domain.repository.LocationRepository
import com.application.victorymap.domain.repository.SealedLocationUserPointResult
import com.application.victorymap.presentation.utils.AVAILABLE
import com.application.victorymap.presentation.utils.NOT_AVAILABLE
import com.application.victorymap.presentation.utils.RESET
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.location.FilteringMode
import com.yandex.mapkit.location.Location
import com.yandex.mapkit.location.LocationListener
import com.yandex.mapkit.location.LocationManager
import com.yandex.mapkit.location.LocationStatus

class LocationRepositoryImpl: LocationRepository {
    private val locationManager: LocationManager = MapKitFactory.getInstance().createLocationManager()

    //Получаем локациб пользователя и отдаем либо Point, либо сообщение с LocationStatus
    override fun fetchUserLocation(callback: (SealedLocationUserPointResult) -> Unit) {
        val listener = object : LocationListener {
            override fun onLocationUpdated(location: Location) {
                val currentPoint = Point(location.position.latitude, location.position.longitude)
                callback(SealedLocationUserPointResult.SuccessPoints(currentPoint))
            }

            override fun onLocationStatusUpdated(locationStatus: LocationStatus) {
                when(locationStatus) {
                    LocationStatus.NOT_AVAILABLE -> callback(SealedLocationUserPointResult.ErrorMessage(NOT_AVAILABLE))
                    LocationStatus.AVAILABLE -> callback(SealedLocationUserPointResult.ErrorMessage(AVAILABLE))
                    LocationStatus.RESET -> callback(SealedLocationUserPointResult.ErrorMessage(RESET))
                }
            }
        }

        locationManager.subscribeForLocationUpdates(
            0.0, // Минимальное время между обновлениями (в миллисекундах), 0.0 означает обновления по мере доступности
            0L, // Минимальное расстояние между обновлениями (в метрах), 0L означает обновления по мере доступности
            0.0, // Минимальная точность обновления (в метрах), 0.0 означает обновления по мере доступности
            true, // Использовать геопривязку
            FilteringMode.OFF, // Режим фильтрации
            listener // Подключаем слушатель обновлений местоположения
        )
    }
}