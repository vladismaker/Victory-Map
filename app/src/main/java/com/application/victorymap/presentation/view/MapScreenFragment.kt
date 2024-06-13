package com.application.victorymap.presentation.view

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.application.victorymap.R
import com.application.victorymap.presentation.viewmodels.MapScreenViewModel
import com.application.victorymap.presentation.viewmodels.MapScreenViewModelFactory
import com.application.victorymap.presentation.utils.NOT_GPS_PERMISSION
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.directions.driving.DrivingRoute
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.map.PolylineMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider

class MapScreenFragment : Fragment() {
    private lateinit var mapView: MapView
    private lateinit var map: Map
    private lateinit var placemarksCollection: MapObjectCollection
    private lateinit var routesCollection: MapObjectCollection
    private lateinit var viewModel: MapScreenViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            //Разрешение на геопозицию предоставлено
            initialize()
        } else {
            //Разрешение на геопозицию не предоставлено, просьба разрешить доступ к геопозиции
            showToast(NOT_GPS_PERMISSION)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.findViewById(R.id.mapview)
        map = mapView.mapWindow.map
        viewModel = ViewModelProvider(
            this,
            MapScreenViewModelFactory(requireContext())
        )[MapScreenViewModel::class.java]

        // Добавил код для наблюдения за LiveData и обновления пользовательского интерфейса
        viewModel.routePoints.observe(viewLifecycleOwner) { points ->
            // Обновление маркеров на карте
            val pointStart = points[0]
            map.move(CameraPosition(pointStart, 17.0f, 0.0f, 0.0f))
            updatePlacemarks(points)
        }

        viewModel.routes.observe(viewLifecycleOwner) { routes ->
            // Обновление маршрутов на карте
            updateRoutes(routes)
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            // Показываем тост уведомление
            showToast(message)
        }

        // Проверка и запрос разрешений
        checkPermissionsFromManifest()
    }

    private fun showToast(textToast: String) {
        Toast.makeText(context, textToast, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionsFromManifest() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(ACCESS_FINE_LOCATION)
            showToast(NOT_GPS_PERMISSION)
        } else {
            initialize()
        }
    }

    private fun initialize() {
        placemarksCollection = map.mapObjects.addCollection()
        routesCollection = map.mapObjects.addCollection()

        // Начать получение местоположения пользователя
        viewModel.fetchUserLocation()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    private fun updatePlacemarks(points: List<Point>) {
        //Чистим маркеры
        placemarksCollection.clear()

        val imageProviderStart = ImageProvider.fromResource(context, R.drawable.icon_mark_red)
        val imageProvider = ImageProvider.fromResource(context, R.drawable.icon_mark_black)

        points.forEachIndexed { index, _ ->
            val imageProviderSelect = if (index == 0) imageProviderStart else imageProvider
            //Добавляем маркеры
            placemarksCollection.addPlacemark().apply {
                geometry = points[index]
                setIcon(imageProviderSelect, IconStyle().apply {
                    scale = if (index == 0) 0.45f else 0.5f
                    zIndex = 20f
                })
            }
        }
    }

    private fun updateRoutes(routes: List<DrivingRoute>) {
        //Чистим маршруты
        routesCollection.clear()

        if (routes.isEmpty()) return

        routes.forEachIndexed { index, route ->
            val polyline = routesCollection.addPolyline(route.geometry)

            //Добавляем маршруты
            if (index == 0) {
                styleMainRoute(polyline)
            } else {
                styleAlternativeRoute(polyline)
            }
        }
    }

    //Настройка отображения основного маршрута
    private fun styleMainRoute(polyline: PolylineMapObject) {
        polyline.apply {
            zIndex = 10f
            setStrokeColor(ContextCompat.getColor(requireContext(), R.color.green))
            strokeWidth = 5f
            outlineColor = ContextCompat.getColor(requireContext(), R.color.gray)
            outlineWidth = 1f
        }
    }

    //Настройка отображения дополнительных маршрутов
    private fun styleAlternativeRoute(polyline: PolylineMapObject) {
        polyline.apply {
            zIndex = 5f
            setStrokeColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
            strokeWidth = 4f
            outlineColor = ContextCompat.getColor(requireContext(), R.color.gray)
            outlineWidth = 1f
        }
    }
}