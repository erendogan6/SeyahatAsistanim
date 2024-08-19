package com.erendogan6.seyahatasistanim.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.erendogan6.seyahatasistanim.data.model.travel.TravelEntity
import com.erendogan6.seyahatasistanim.data.repository.TravelRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TravelViewModel(
    private val travelRepository: TravelRepository,
) : ViewModel() {
    private val _travelInfo = MutableStateFlow<TravelEntity?>(null)
    val travelInfo: StateFlow<TravelEntity?> = _travelInfo

    fun saveTravelInfo(travelEntity: TravelEntity) {
        viewModelScope.launch {
            travelRepository.saveTravelInfo(travelEntity)
        }
    }

    fun loadLastTravelInfo() {
        viewModelScope.launch {
            _travelInfo.value = travelRepository.getLastTravelInfo()
        }
    }
}
