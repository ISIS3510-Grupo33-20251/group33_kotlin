package com.example.universe.domain.repositories

import kotlinx.coroutines.flow.Flow

interface NetworkConnectivityObserver {
    fun observe(): Flow<NetworkStatus>
}

enum class NetworkStatus {
    Available, Unavailable, Lost, Losing
}