package tech.jhavidit.remindme.repository

import androidx.lifecycle.MutableLiveData
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel

class MainActivityRepository {

    val locationModel = MutableLiveData<LocationModel>()
    val notesModel = MutableLiveData<NotesModel>()

    fun getLocation(location: LocationModel) {
        locationModel.value = location
    }

    fun getNotes(note: NotesModel) {
        notesModel.value = note
    }

    fun clearData() {
        locationModel.value = null
        notesModel.value = null
    }


}