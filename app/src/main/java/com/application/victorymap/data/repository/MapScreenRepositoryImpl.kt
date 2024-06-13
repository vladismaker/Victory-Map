package com.application.victorymap.data.repository

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.application.victorymap.domain.repository.MapScreenRepository

class MapScreenRepositoryImpl(private val context: Context): MapScreenRepository {
    //Получаем подключен ли на устройстве интернет (true/false)
    override fun getInternetStatus(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNet = connectivityManager.activeNetwork
        if (activeNet != null) {
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNet)
            return networkCapabilities != null && networkCapabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }

        return false
    }
}