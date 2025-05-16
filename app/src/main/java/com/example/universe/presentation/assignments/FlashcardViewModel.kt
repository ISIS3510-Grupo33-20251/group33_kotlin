package com.example.universe.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.models.Flashcard
import com.example.universe.data.repositories.FlashcardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashcardViewModel @Inject constructor(
    private val repository: FlashcardRepository
) : ViewModel() {

    private val _flashcards = MutableStateFlow<List<Flashcard>>(emptyList())
    val flashcards: StateFlow<List<Flashcard>> = _flashcards

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchFlashcards(userId: String, subject: String) {
        viewModelScope.launch {
            _loading.value = true
            _error.value = null
            try {
                val data = repository.getFlashcards(userId, subject)
                _flashcards.value = data
            } catch (e: Exception) {
                _error.value = "Error al obtener flashcards: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _loading.value = false
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearAllCache()
            _flashcards.value = emptyList()
        }
    }
}
