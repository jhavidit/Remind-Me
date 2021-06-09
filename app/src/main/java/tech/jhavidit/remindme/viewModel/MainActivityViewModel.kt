package tech.jhavidit.remindme.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import tech.jhavidit.remindme.repository.MainActivityRepository

class MainActivityViewModel : ViewModel() {
    val locationModel: LiveData<LocationModel>
    val notesModel: LiveData<NotesModel>
    private val repository: MainActivityRepository = MainActivityRepository()

    init {
        this.locationModel = repository.locationModel
        this.notesModel = repository.notesModel
    }


    fun getLocation(location: LocationModel) {
        repository.getLocation(location)
    }

    fun getNotes(note: NotesModel) {
        repository.getNotes(note)
    }

    fun clearData() {
        repository.clearData()
    }

}