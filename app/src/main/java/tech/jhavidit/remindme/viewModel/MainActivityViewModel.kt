package tech.jhavidit.remindme.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel

class MainActivityViewModel : ViewModel() {
     val locationModel = MutableLiveData<LocationModel>()
     val notesModel = MutableLiveData<NotesModel>()

    fun getLocation(location: LocationModel) {
        viewModelScope.launch(Dispatchers.IO) {
            locationModel.value = location
        }
    }

    fun getNotes(note: NotesModel) {
        viewModelScope.launch(Dispatchers.IO) {
            notesModel.value = note
        }
    }

    fun clearData()
    {
        locationModel.value = null
        notesModel.value = null
    }


}