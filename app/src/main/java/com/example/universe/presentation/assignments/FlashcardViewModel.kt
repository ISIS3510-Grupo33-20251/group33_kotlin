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

    fun fetchFlashcards(userId: String, subject: String) {
        viewModelScope.launch {
            try {
                _flashcards.value = repository.getFlashcards(userId, subject)
            } catch (e: Exception) {
                _error.value = "Error al obtener flashcards: ${e.localizedMessage}"
            }
        }
    }
}
