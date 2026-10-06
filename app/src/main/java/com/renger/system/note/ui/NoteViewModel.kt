package com.renger.system.note.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renger.system.note.data.Note
import com.renger.system.note.data.NoteDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = NoteDatabase.getDatabase(app).noteDao()

    val notes = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getNote(id: Int, callback: (Note?) -> Unit) {
        viewModelScope.launch { callback(dao.getNoteById(id)) }
    }

    fun save(note: Note) = viewModelScope.launch {
        if (note.id == 0) dao.insertNote(note) else dao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch { dao.deleteNote(note) }
}