package com.example.networklibrary.network.monitor

/**
 * Абстракция для проверки сетевого подключения
 */
interface NetworkMonitor {
    fun isConnected(): Boolean
}