package com.codelabs.gardahijau.ui.notifications

import com.codelabs.gardahijau.model.remote.network.pesanGalat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codelabs.gardahijau.model.remote.response.notification.NotificationItem
import com.codelabs.gardahijau.model.remote.response.notification.NotificationResponse
import com.codelabs.gardahijau.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _notifData = MutableLiveData<NotificationResponse>()
    val notifData: LiveData<NotificationResponse> = _notifData

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _filteredData = MutableLiveData<List<NotificationItem>>()
    val filteredData: LiveData<List<NotificationItem>> = _filteredData
    private var originalList: List<NotificationItem> = emptyList()

    fun setFilter(filter: FilterType) {
        val filtered = when (filter) {
            FilterType.SEMUA -> originalList
            FilterType.BELUM -> originalList.filter { !it.isRead }
            FilterType.DIBACA -> originalList.filter { it.isRead }
        }
        _filteredData.value = filtered
    }

    fun getNotification() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = notificationRepository.notificationRepository()
                _notifData.value = response
                originalList = response.data
                _filteredData.value = originalList // default = semua
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = e.pesanGalat()
                _isLoading.value = false
            }
        }
    }

    fun markNotificationAsRead(item: NotificationItem) {
        viewModelScope.launch {
            val result = notificationRepository.markAsRead(item.id)

            if (result) {
                item.isRead = true
                setFilter(FilterType.SEMUA) // refresh tampilan
            }
        }
    }


    enum class FilterType {
        SEMUA, BELUM, DIBACA
    }

}
