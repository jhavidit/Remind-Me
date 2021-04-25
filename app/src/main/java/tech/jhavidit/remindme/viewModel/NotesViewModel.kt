package tech.jhavidit.remindme.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<NotesModel>>
    val notesCount : LiveData<Int>
    private val repository: NotesRepository

    init {
        val userDao = NotesDatabase.getDatabase(
                application
        ).userDao()
        repository = NotesRepository(userDao)
        readAllData = repository.readAllData
        notesCount = repository.notesCount
    }

    fun addNotes(notes: NotesModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNotes(notes)
        }
    }

    fun updateNotes(notes: NotesModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNotes(notes)
        }
    }

    fun deleteNotes(notes: NotesModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNotes(notes)
        }
    }

    fun deleteAllNotes() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllNotes()
        }
    }

}