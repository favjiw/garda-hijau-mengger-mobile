package com.codelabs.gardahijau.model.remote.response.notification

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class NotificationResponse(
    val success: Boolean,
    val data: List<NotificationItem>
)

@Parcelize
data class NotificationItem(
    val id: Int,
    val userId: Int,
    val title: String,
    val message: String,
    val type: String,
    val createdAt: String,
    var isRead: Boolean
): Parcelable


data class MarkAsReadResponse(
    val success: Boolean,
    val updated: Int
)

