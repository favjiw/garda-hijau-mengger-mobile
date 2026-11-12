package com.codelabs.gardahijau.repository

import com.codelabs.gardahijau.model.remote.response.RemoteDataSource
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.AddHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.hitoryPencacahan.GetHistoryPencacahanResponse
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val remoteDataSource: RemoteDataSource
) {
    suspend fun notificationRepository(): NotificationResponse {
        return remoteDataSource.getNotification()
    }

    suspend fun markAsRead(id: Int): Boolean {
        return remoteDataSource.markAsRead(id)
    }

}