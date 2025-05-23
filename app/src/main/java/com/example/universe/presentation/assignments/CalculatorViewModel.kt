package com.example.universe.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.models.CalculatorSubjectDto
import com.example.universe.data.repositories.CalculatorRepository
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val repository: CalculatorRepository
) : ViewModel() {

    private val _subjects = MutableStateFlow<List<CalculatorSubjectDto>>(emptyList())
    val subjects: StateFlow<List<CalculatorSubjectDto>> = _subjects

    private val _selectedSubject = MutableStateFlow<CalculatorSubjectDto?>(null)
    val selectedSubject: StateFlow<CalculatorSubjectDto?> = _selectedSubject

    private val _networkStatus = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()


    fun loadSubjects(ownerId: String) {
        viewModelScope.launch {
            _subjects.value = repository.getSubjectsByUser(ownerId)
        }
    }

    fun selectSubject(subjectId: String) {
        viewModelScope.launch {
            _selectedSubject.value = repository.getSubject(subjectId)
        }
    }

    // ViewModel
    fun createSubject(subject: CalculatorSubjectDto, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.createSubject(subject)
                onSuccess()
            } catch (e: Exception) {
                // Manejo de error si deseas
            }
        }
    }


    fun updateSubject(subjectId: String, updatedSubject: CalculatorSubjectDto) {
        viewModelScope.launch {
            repository.updateSubject(subjectId, updatedSubject)
            loadSubjects(updatedSubject.owner_id)
        }
    }

    fun deleteSubject(subjectId: String, ownerId: String) {
        viewModelScope.launch {
            repository.deleteSubject(subjectId)
            loadSubjects(ownerId)
        }
    }

    fun observeNetworkAndSync(networkObserver: NetworkConnectivityObserver) {
        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                _networkStatus.value = status
                if (status == NetworkStatus.Available) {
                    repository.syncPendingSubjects()
                }
            }
        }
    }
}