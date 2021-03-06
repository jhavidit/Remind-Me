package tech.jhavidit.remindme.viewModel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.repository.NotesRepository
import tech.jhavidit.remindme.room.NotesDatabase
import tech.jhavidit.remindme.util.log

class NotesViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<NotesModel>>
    val notesCount: LiveData<Int>
    val createdId: LiveData<Int>
    private val repository: NotesRepository

    init {
        val userDao = NotesDatabase.getDatabase(
            application
        ).userDao()
        repository = NotesRepository(userDao)
        readAllData = repository.readAllData
        notesCount = repository.notesCount
        createdId = repository.createdId
    }

    fun addNotes(notes: NotesModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNotes(notes)
        }
    }

    fun selectedNote(id: Int): LiveData<List<NotesModel>> {

        return repository.selectedNote(id).asLiveData()

    }

    fun searchNote(searchQuery: String?): LiveData<List<NotesModel>> {
        return repository.searchNote(searchQuery).asLiveData()
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

}