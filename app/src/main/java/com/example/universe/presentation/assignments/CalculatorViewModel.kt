package com.example.universe.presentation.assignments

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.models.CalculatorSubjectDto
import com.example.universe.data.repositories.CalculatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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


    fun loadSubjects(ownerId: String) {
        viewModelScope.launch {
            try {
                val result = repository.getSubjectsByUser(ownerId)
                _subjects.value = result
            } catch (e: Exception) {
                Log.e("CalculatorViewModel", "Error loading subjects", e)
                _subjects.value = emptyList()
            }
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
}