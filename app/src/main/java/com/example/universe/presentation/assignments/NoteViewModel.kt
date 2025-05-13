package com.example.universe.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.universe.data.models.NoteDto
import com.example.universe.data.repositories.NoteRepository
import com.example.universe.domain.repositories.NetworkConnectivityObserver
import com.example.universe.domain.repositories.NetworkStatus
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

    fun createNote(note: NoteDto, userId: String, isOffline: Boolean = false) {
        if (note.title.isNullOrBlank() || note.content.isNullOrBlank()) {
            _noteState.value = NoteState.Error("Title, subject or content can't be empty")
            return
        }

        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                if (isOffline) {
                    noteRepository.createNoteOffline(note)
                } else {
                    val createdNote: Response<NoteDto> = noteRepository.createNote(note)
                    if (createdNote.code() == 200) {
                        val userIdR = createdNote.body()?.owner_id
                        val noteId = createdNote.body()?.id
                        if (userIdR != null && noteId != null) {
                            noteRepository.addNoteToUser(userId, noteId)
                        }
                    }
                }
                getNotes(userId, isOffline)
            } catch (error: Exception) {
                // fallback a local si ocurre error
                noteRepository.createNoteOffline(note)
                getNotes(userId, isOffline = true)
                _noteState.value = NoteState.Error("Error online: guardado offline")
            }
        }
    }

    fun getNotes(userId: String, isOffline: Boolean = false) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                if (isOffline) {
                    val localNotes = noteRepository.getNotesOffline(userId)
                    _noteState.value = NoteState.Success(localNotes)
                } else {
                    val response = noteRepository.getNotes(userId)
                    if (response.isSuccessful) {
                        val allNotes = response.body() ?: emptyList()
                        _noteState.value = NoteState.Success(allNotes)
                    } else {
                        // fallback a local
                        val localNotes = noteRepository.getNotesOffline(userId)
                        _noteState.value = NoteState.Success(localNotes)
                    }
                }
            } catch (error: Exception) {
                val localNotes = noteRepository.getNotesOffline(userId)
                _noteState.value = NoteState.Success(localNotes)
            }
        }
    }

    fun updateNote(id: String, note: NoteDto, userId: String, isOffline: Boolean = false) {
        if (note.title.isNullOrBlank() || note.content.isNullOrBlank()) {
            _noteState.value = NoteState.Error("Title, subject or content can't be empty")
            return
        }

        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                if (isOffline) {
                    noteRepository.updateNoteOffline(note.copy(id = id))
                } else {
                    val updatedNote = noteRepository.updateNote(id, note)
                    if (!updatedNote.isSuccessful) {
                        noteRepository.updateNoteOffline(note.copy(id = id))
                    }
                }
            } catch (e: Exception) {
                noteRepository.updateNoteOffline(note.copy(id = id))
            }
            getNotes(userId, isOffline)
        }
    }


    fun deleteNote(id: String, userId: String, isOffline: Boolean = false) {
        _noteState.value = NoteState.Loading
        viewModelScope.launch {
            try {
                if (isOffline) {
                    noteRepository.deleteNoteOffline(id)
                } else {
                    val response = noteRepository.deleteNote(id)
                    if (!response.isSuccessful) {
                        noteRepository.deleteNoteOffline(id)
                    }
                }
            } catch (e: Exception) {
                noteRepository.deleteNoteOffline(id)
            }
            getNotes(userId, isOffline)
        }
    }

    fun observeNetworkAndSync(networkObserver: NetworkConnectivityObserver) {
        viewModelScope.launch {
            networkObserver.observe().collect { status ->
                if (status == NetworkStatus.Available) {
                    noteRepository.syncPendingNotes()
                }
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