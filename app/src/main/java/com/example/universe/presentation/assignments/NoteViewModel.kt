package com.example.universe.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.models.NoteDto
import com.example.universe.data.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class NoteViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {

    private val _noteState = MutableStateFlow<NoteState>(NoteState.Initial)
    val noteState: StateFlow<NoteState> = _noteState.asStateFlow()

    fun createNote(note: NoteDto, userId: String) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                val createdNote: Response<NoteDto> = noteRepository.createNote(note)
                getNotes(userId)

                if(createdNote.code() == 200){

                    val userIdR = createdNote.body()?.owner_id
                    val noteId = createdNote.body()?.id
                    if (userIdR != null && noteId != null) {
                        noteRepository.addNoteToUser(userId, noteId )
                    }

                }
            } catch (error: Exception) {
                _noteState.value = NoteState.Error(error.message ?: "Unknown error")
            }
        }
    }


    fun getNotes(userId: String) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                val response = noteRepository.getNotes(userId)
                if (response.isSuccessful) {
                    val allNotes = response.body() ?: emptyList()
                    _noteState.value = NoteState.Success(allNotes)
                } else {
                    _noteState.value = NoteState.Error("Error fetching notes: ${response.message()}")
                }
            } catch (error: Exception) {
                _noteState.value = NoteState.Error(error.message ?: "Unknown error")
            }
        }
    }


    fun updateNote(id: String, note: NoteDto, userId: String) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                val updatedNote = noteRepository.updateNote(id, note)
                if (updatedNote.isSuccessful) {
                    getNotes(userId) // Recargar la lista de notas después de actualizar
                } else {
                    _noteState.value = NoteState.Error("Failed to update note")
                }
            } catch (error: Exception) {
                _noteState.value = NoteState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun deleteNote(id: String, userId: String) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                val response = noteRepository.deleteNote(id)
                if (response.isSuccessful) {
                    getNotes(userId)
                } else {
                    _noteState.value = NoteState.Error("Failed to delete note")
                }
            } catch (error: Exception) {
                _noteState.value = NoteState.Error(error.message ?: "Unknown error")
            }
        }
    }


}


sealed class NoteState {
    object Initial : NoteState()
    object Loading : NoteState()
    data class Success(val notes: List<NoteDto>) : NoteState()
    data class Error(val message: String) : NoteState()
}