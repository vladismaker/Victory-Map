package com.application.victorymap.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.application.victorymap.data.repository.RouteRepository
import com.application.victorymap.data.repository.LocationRepositoryImpl
import com.application.victorymap.data.repository.MapScreenRepositoryImpl
import com.application.victorymap.domain.repository.SealedLocationListPointsResult
import com.application.victorymap.domain.usecase.GetInternetStatusUseCase
import com.application.victorymap.domain.usecase.GetUserLocationPointUseCase
import com.application.victorymap.presentation.utils.CHECK_INTERNET
import com.application.victorymap.presentation.utils.ROUTES_REQUEST_ERROR
import com.application.victorymap.presentation.utils.ROUTES_REQUEST_UNKNOWN_ERROR
import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.RequestPointType
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions
import com.yandex.mapkit.geometry.Point
import com.yandex.runtime.Error
import com.yandex.runtime.network.NetworkError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapScreenViewModel(context: Context) : ViewModel() {
    private val mapScreenRepository = MapScreenRepositoryImpl(context)
    private val getInternetStatusUseCase =
        GetInternetStatusUseCase(mapScreenRepository = mapScreenRepository)

    private val locationRepository = LocationRepositoryImpl()
    private val getUserLocationPointUseCase = GetUserLocationPointUseCase(locationRepository = locationRepository)

    init {
        // Наблюдаем за изменениями в sealedLocationListPointsResultLiveData
        getUserLocationPointUseCase.sealedLocationListPointsResultLiveData.observeForever { result ->
            when (result) {
                is SealedLocationListPointsResult.SuccessPointsSealed -> {
                    val points = result.points
                    _routePoints.value = points
                    requestRoutes(points)
                }

                is SealedLocationListPointsResult.ErrorMessage -> {
                    val message = result.message
                    _toastMessage.value = message
                }
            }
        }
    }

    private var coroutine = CoroutineScope(Dispatchers.IO)

    private var drivingSession: DrivingSession? = null


    private val routeRepository = RouteRepository()

    private val _routePoints = MutableLiveData<List<Point>>()
    val routePoints: LiveData<List<Point>> = _routePoints

    private val _routes = MutableLiveData<List<DrivingRoute>>()
    val routes: LiveData<List<DrivingRoute>> = _routes

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> = _toastMessage


    private val drivingRouteListener = object : DrivingSession.DrivingRouteListener {
        override fun onDrivingRoutes(drivingRoutes: MutableList<DrivingRoute>) {
            _routes.postValue(drivingRoutes)
            drivingSession?.cancel()
        }

        override fun onDrivingRoutesError(error: Error) {
            when (error) {
                is NetworkError -> _toastMessage.postValue(ROUTES_REQUEST_ERROR)
                else -> _toastMessage.postValue(ROUTES_REQUEST_UNKNOWN_ERROR)
            }
            drivingSession?.cancel()
        }
    }

    fun fetchUserLocation() {
        coroutine.launch {
            //Проверяем интернет
            val internetStatus = async { getInternetStatusUseCase.execute() }.await()

            withContext(Dispatchers.Main) {
                if (internetStatus) {
                    //Интернет есть, получаем локацию пользователя
                    getUserLocationPointUseCase.execute()
                } else {
                    //Интернета нет, уведомляем пользователя
                    _toastMessage.value = CHECK_INTERNET
                }
            }
        }
    }

    //Настройки отображения маршрута
    private fun requestRoutes(points: List<Point>) {
        if (points.size < 2) return
        val requestPoints = buildList {
            add(RequestPoint(points.first(), RequestPointType.WAYPOINT, null, null))
            addAll(
                points.subList(1, points.size - 1)
                    .map { RequestPoint(it, RequestPointType.VIAPOINT, null, null) }
            )
            add(RequestPoint(points.last(), RequestPointType.WAYPOINT, null, null))
        }

        val drivingOptions = DrivingOptions().apply {
            routesCount = 3
        }

        val vehicleOptions = VehicleOptions()

        drivingSession = routeRepository.requestRoutes(
            requestPoints,
            drivingOptions,
            vehicleOptions,
            drivingRouteListener
        )
    }

    override fun onCleared() {
        super.onCleared()
        coroutine.cancel()
    }
}