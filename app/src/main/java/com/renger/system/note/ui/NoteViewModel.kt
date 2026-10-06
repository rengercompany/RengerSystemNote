package com.renger.system.note.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.renger.system.note.data.Folder
import com.renger.system.note.data.Note
import com.renger.system.note.data.NoteDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NoteViewModel(app: Application) : AndroidViewModel(app) {
    private val noteDao = NoteDatabase.getDatabase(app).noteDao()
    private val folderDao = NoteDatabase.getDatabase(app).folderDao()

    val notes = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders = folderDao.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getNote(id: Int, callback: (Note?) -> Unit) {
        viewModelScope.launch { callback(noteDao.getNoteById(id)) }
    }

    fun save(note: Note) = viewModelScope.launch {
        if (note.id == 0) noteDao.insertNote(note) else noteDao.updateNote(note)
    }

    fun delete(note: Note) = viewModelScope.launch { noteDao.deleteNote(note) }

    fun addFolder(name: String, color: Int) = viewModelScope.launch {
        folderDao.insertFolder(Folder(name = name, color = color))
    }

    fun deleteFolder(folder: Folder) = viewModelScope.launch {
        folderDao.detachNotes(folder.id) // заметки остаются, но без папки
        folderDao.deleteFolder(folder)
    }
}