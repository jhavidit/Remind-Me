package tech.jhavidit.remindme.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.repository.LocationRepository
import tech.jhavidit.remindme.room.NotesDatabase

class LocationViewModel(application: Application) : AndroidViewModel(application) {

    val readAllData: LiveData<List<LocationModel>>
    private val repository: LocationRepository

    init {
        val locationDao = NotesDatabase.getDatabase(
            application
        ).locationDao()
        repository = LocationRepository(locationDao)
        readAllData = repository.readAllData
    }

    fun addLocation(location: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addLocation(location)
        }
    }

    fun updateLocation(location: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLocation(location)
        }
    }

    fun deleteLocation(location: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteLocation(location)
        }
    }

    fun deleteAllLocation() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllLocation()
        }
    }
}