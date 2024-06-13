package com.application.victorymap.data.repository

import com.yandex.mapkit.RequestPoint
import com.yandex.mapkit.directions.DirectionsFactory
import com.yandex.mapkit.directions.driving.DrivingOptions
import com.yandex.mapkit.directions.driving.DrivingRouterType
import com.yandex.mapkit.directions.driving.DrivingSession
import com.yandex.mapkit.directions.driving.VehicleOptions

class RouteRepository {
    private val drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(DrivingRouterType.COMBINED)

    fun requestRoutes(
        requestPoints: List<RequestPoint>,
        drivingOptions: DrivingOptions,
        vehicleOptions: VehicleOptions,
        listener: DrivingSession.DrivingRouteListener
    ):DrivingSession {
        return drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, listener)
    }
}